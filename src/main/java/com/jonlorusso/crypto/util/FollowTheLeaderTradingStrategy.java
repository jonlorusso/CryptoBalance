package com.jonlorusso.crypto.util;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jonlorusso.crypto.entity.TickerGroup;

@Component
public class FollowTheLeaderTradingStrategy implements TradingStrategy {

	@Value("${tradingStrategy.buyThreshold}")
	private int buyThreshold;
	
	@Value("${tradingStrategy.sellThreshold}")
	private int sellThreshold;
	
	@Override
	public Optional<SignalType> getSignalType(TickerGroup tickerGroup) {
		long gainCount = tickerGroup.getTickers().stream().filter(t -> t.getDayPercentChange() > 0.0).count();
		
		if (gainCount > sellThreshold) {
			return Optional.of(SignalType.SELL);
		}	
		
		if (gainCount < buyThreshold) {
			return Optional.of(SignalType.BUY);
		}
		
		return Optional.empty();
	}
}
