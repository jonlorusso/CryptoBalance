package com.jonlorusso.crypto.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.jonlorusso.crypto.entity.Currency;
import com.jonlorusso.crypto.entity.Ticker;
import com.jonlorusso.crypto.entity.TickerGroup;
import com.jonlorusso.crypto.entity.Trade;
import com.jonlorusso.crypto.entity.Trade.TradeType;
import com.jonlorusso.crypto.entity.TradeGroup;
import com.jonlorusso.crypto.repository.CurrencyRepository;
import com.jonlorusso.crypto.repository.TickerGroupRepository;
import com.jonlorusso.crypto.repository.TradeGroupRepository;
import com.jonlorusso.crypto.repository.TradeRepository;

import jersey.repackaged.com.google.common.collect.Lists;

@Transactional
@Service
public class TradeService {

	private TradeGroupRepository tradeGroupRepository;
	private TickerGroupRepository tickerGroupRepository;
	private TradeRepository tradeRepository;
	private CurrencyRepository currencyRepository;
	
	//FIXME propertiesFile
	private List<String> excludedCurrencies = Lists.newArrayList("USDT"); 

	public TradeService(TickerGroupRepository tickerGroupRepository, TradeGroupRepository tradeGroupRepository, TradeRepository tradeRepository, CurrencyRepository currencyRepository) {
		this.tickerGroupRepository = tickerGroupRepository;
		this.tradeGroupRepository = tradeGroupRepository;
		this.tradeRepository = tradeRepository;
		this.currencyRepository = currencyRepository;
	}
	
	public Map<Currency, Double> computeHoldings() {
		return computeHoldingsAsOf(Instant.now().getEpochSecond());
	}
	
	public Map<Currency, Double> computeHoldingsAsOf(long timestamp) {
		TradeGroup tradeGroup = tradeGroupRepository.findTopByTimestampLessThanOrderByTimestampDesc(timestamp);
		return computeHoldings(tradeGroup);
	}
	
	public Map<Currency, Double> computeHoldings(TradeGroup tradeGroup) {
		Map<Currency, Double> holdings = new TreeMap<>();
		
		for (Trade trade : tradeGroup.getTrades()) {
			if (trade.getTradeType() == TradeType.BUY) {
				Currency currency = trade.getBaseCurrency();
				Double quantity = holdings.get(currency);
				if (quantity == null) {
					quantity = 0.0;
				}
				holdings.put(currency, quantity + trade.getQuantity());
			}
			
			if (trade.getTradeType() == TradeType.SELL) {
				Currency currency = trade.getQuoteCurrency();
				Double quantity = holdings.get(currency);
				if (quantity == null) {
					quantity = 0.0;
				}
				holdings.put(currency, quantity + (trade.getQuantity() * trade.getPrice()));
			}
		}
		
		return holdings;
	}
	
	public Double getHoldingsValueInBTC(Map<Currency, Double> holdings, long timestamp) {
		TickerGroup tickerGroup = tickerGroupRepository.findTopByTimestampLessThanOrderByTimestampDesc(timestamp);
		if (tickerGroup == null) {
			return 0.0;
		}
		return holdings.keySet().stream().mapToDouble(c -> tickerGroup.getTicker(c).getPriceBTC() * holdings.get(c)).sum();
	}
	
	private boolean isExcluded(Currency currency) {
		for (String excludedCurrency : excludedCurrencies) {
			if (currency.getSymbol().equals(excludedCurrency)) {
				return true;
			}
		}
		return false;
	}
	
	public TradeGroup buy() {
		System.out.println("buying");
		TradeGroup lastTradeGroup = tradeGroupRepository.findTopByOrderByTimestampDesc();
		if (lastTradeGroup == null) {
			return null;
		}
		
		Currency bitcoin = currencyRepository.findOneBySymbol("BTC");
		Double bitcoinQuantity = lastTradeGroup.getBalances().get(bitcoin);
		if (bitcoinQuantity == null || bitcoinQuantity <= 0.0) {
			return null;
		}
		
		long timestamp = Instant.now().getEpochSecond();
		TickerGroup tickerGroup = tickerGroupRepository.findTopByOrderByTimestampDesc();
		TradeGroup tradeGroup = new TradeGroup(timestamp, tickerGroup);
		
		Map<Currency, Ticker> tradeableCurrencies = new HashMap<>();
		Double totalMarketCap = 0.0; 

		for (Ticker ticker : tickerGroup.getTickers()) {
			Currency currency = ticker.getCurrency();
			if (!currency.isBitcoin() && !isExcluded(currency)) {
				tradeableCurrencies.put(currency, ticker);
				totalMarketCap += currency.getMarketCapUSD();
			}
		}
		
		for (Currency currency : tradeableCurrencies.keySet()) {
			Ticker ticker = tradeableCurrencies.get(currency);
			double quantity = (( currency.getMarketCapUSD() / totalMarketCap ) * bitcoinQuantity) / ticker.getPriceBTC();
			Trade trade = new Trade(timestamp, currency, bitcoin, ticker.getPriceBTC(), quantity, TradeType.BUY);
			tradeGroup.addTrade(trade); //TODO need to save this?
			tradeRepository.save(trade);
		}
		
		tradeGroupRepository.save(tradeGroup);
		return tradeGroup;
		
	}

	public TradeGroup sell() {
		System.out.println("selling");
		TradeGroup lastTradeGroup = tradeGroupRepository.findTopByOrderByTimestampDesc();
		if (lastTradeGroup == null) {
			return null;
		}
		
		Currency bitcoin = currencyRepository.findOneBySymbol("BTC");
		Map<Currency, Double> balances = lastTradeGroup.getBalances();
		
		Double bitcoinQuantity = balances.get(bitcoin);
		if (bitcoinQuantity != null) {
			return null;
		}
		
		TickerGroup tickerGroup = tickerGroupRepository.findTopByOrderByTimestampDesc();
		long timestamp = Instant.now().getEpochSecond();
		TradeGroup tradeGroup = new TradeGroup(timestamp, tickerGroup);
		
		for (Ticker ticker : tickerGroup.getTickers()) {
			Currency currency = ticker.getCurrency();
			Double currencyHoldings = balances.get(currency);
			if (!currency.isBitcoin() && !isExcluded(currency) && (currencyHoldings != null) && (currencyHoldings > 0.0)) {
				Trade trade = new Trade(timestamp, currency, bitcoin, ticker.getPriceBTC(), balances.get(currency), TradeType.SELL);
				tradeGroup.addTrade(trade); //TODO need to save this?
				tradeRepository.save(trade);
			}
		}
		
		tradeGroupRepository.save(tradeGroup); //TODO need to move this up?
		return tradeGroup;
	}

	public void initializeBalance() {
		TradeGroup lastTradeGroup = tradeGroupRepository.findTopByOrderByTimestampDesc();
		if (lastTradeGroup == null) {
			long timestamp = Instant.now().getEpochSecond();
			TradeGroup tradeGroup = new TradeGroup(timestamp, null);
			
			Currency bitcoin = currencyRepository.findOneBySymbol("BTC");
			Currency ripple = currencyRepository.findOneBySymbol("XRP");
			
			Trade trade = new Trade(timestamp, ripple, bitcoin, 0.1, 10.0, TradeType.SELL);
			tradeRepository.save(trade);
			
			tradeGroup.addTrade(trade);
			tradeGroupRepository.save(tradeGroup);
		}
	}
}
