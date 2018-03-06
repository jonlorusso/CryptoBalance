package com.jonlorusso.crypto.util;

import java.util.Optional;

import com.jonlorusso.crypto.entity.TickerGroup;

public interface TradingStrategy {

	public static enum SignalType { BUY, SELL }

	Optional<SignalType> getSignalType(TickerGroup tickerGroup);
}