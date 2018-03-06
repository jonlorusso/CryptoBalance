package com.jonlorusso.crypto.service;

import java.time.Instant;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerStatistics;
import com.jonlorusso.crypto.entity.Currency;
import com.jonlorusso.crypto.entity.Ticker;
import com.jonlorusso.crypto.entity.TickerGroup;
import com.jonlorusso.crypto.repository.CurrencyRepository;
import com.jonlorusso.crypto.repository.TickerGroupRepository;
import com.jonlorusso.crypto.repository.TickerRepository;
import com.jonlorusso.crypto.util.TradingStrategy;
import com.jonlorusso.crypto.util.TradingStrategy.SignalType;

@Transactional
@Service
public class TickerService {

	private static final Logger log = LoggerFactory.getLogger(TickerService.class);
	
	@Autowired
	private BinanceApiRestClient binanceApiRestClient;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private TickerRepository tickerRepository;

	@Autowired
	private TickerGroupRepository tickerGroupRepository;

	@Autowired
	private 	TradeService tradeService;
	
	@Autowired
	private TradingStrategy tradingStrategy;
	

	@Scheduled(fixedRate = 60000)
	public TickerGroup storeTickers() {
		long timestamp = Instant.now().getEpochSecond();
		TickerGroup tickerGroup = new TickerGroup(timestamp);

		for (Currency currency : currencyRepository.findAll()) {
			if (!currency.isBitcoin()) {
				try {
					TickerStatistics tickerStatistics = binanceApiRestClient
							.get24HrPriceStatistics(String.format("%sBTC", currency.getSymbol()));
					if (tickerStatistics != null) {
						Ticker ticker = new Ticker(timestamp, currency, Double.valueOf(tickerStatistics.getLastPrice()),
								Double.valueOf(tickerStatistics.getVolume()),
								Double.valueOf(tickerStatistics.getPriceChangePercent()));
						tickerGroup.addTicker(ticker);
						tickerRepository.save(ticker);
					}
				} catch (Exception e) {
					log.error("Exception caught while processing ticker for " + currency.getSymbol() + ": " + e.getMessage());
				}
			}
		}

		tickerGroupRepository.save(tickerGroup);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				Optional<SignalType> signalType = tradingStrategy.getSignalType(tickerGroup);
				signalType.ifPresent(s -> { if (s.equals(SignalType.BUY)) tradeService.buy(); });
				signalType.ifPresent(s -> { if (s.equals(SignalType.SELL)) tradeService.sell(); });
			}
		}).start();
		
		return tickerGroup;
	}
}
