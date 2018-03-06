package com.jonlorusso.crypto.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TradeGroupTest {

	@Test
	public void testGetBTCValue() throws Exception {
		TradeGroup tradeGroup = new TradeGroup();
		tradeGroup.addTrade(new Trade(0, null, null, 10.0, 1.0, null));
		tradeGroup.addTrade(new Trade(0, null, null, 5.0, 2.0, null));
		tradeGroup.addTrade(new Trade(0, null, null, 1.0, 10.0, null));
		
		assertEquals(30.0, tradeGroup.getBTCValue(), 0.0);
	}
}
