package com.jonlorusso.crypto.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.jonlorusso.crypto.entity.Currency;
import com.jonlorusso.crypto.repository.CurrencyRepository;
import com.lucadev.coinmarketcap.CoinMarketCap;
import com.lucadev.coinmarketcap.model.CoinMarket;
import com.lucadev.coinmarketcap.model.CoinMarketList;

@Transactional
@Service
public class CurrencyService {

	@Autowired
	private CurrencyRepository currencyRepository;
	
	private Currency updateOrCreateCurrency(CoinMarket coinMarket) {
		Currency currency = currencyRepository.findOneBySymbol(coinMarket.getSymbol());
		if (currency == null) {
			currency = new Currency(coinMarket.getName(), coinMarket.getSymbol(), coinMarket.getRank(), coinMarket.getMarketCapUSD(), coinMarket.getAvailableSupply(), coinMarket.getTotalSupply(),  coinMarket.getMaxSupply());
		} else {
			currency.setAvailableSupply(coinMarket.getRank());
			currency.setAvailableSupply(coinMarket.getMarketCapUSD());
			currency.setAvailableSupply(coinMarket.getAvailableSupply());
			currency.setAvailableSupply(coinMarket.getTotalSupply());
			currency.setAvailableSupply(coinMarket.getMaxSupply());
		}
		currencyRepository.save(currency);
		return currency;
	}
	
	@Scheduled(fixedRate = 60000)
	public void updateMarketCaps() {
		CoinMarketList coinMarketList = CoinMarketCap.ticker().setLimit(20).get();
		coinMarketList.getMarkets().stream().forEach(cm -> updateOrCreateCurrency(cm));
	}
}
