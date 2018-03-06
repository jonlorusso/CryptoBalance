package com.jonlorusso.crypto.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TradeTest {

	@Test
	public void testTradeLongCurrencyCurrencyDoubleDoubleTradeType() throws Exception {
		assertEquals(10.0, new Trade(0, null, null, 5.0, 2.0, null).getBTCValue(), 0.0);
	}

}
