package com.jonlorusso.crypto.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.jonlorusso.crypto.entity.Trade.TradeType;

@Entity
@Table(indexes = @Index(columnList = "timestamp"))
public class TradeGroup implements Comparable<TradeGroup> {

    @Id
    @GeneratedValue
    private long id;
    private long timestamp;
    
    @OneToOne(fetch = FetchType.LAZY)
    private TickerGroup tickerGroup;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("baseCurrency")
    private List<Trade> trades = new ArrayList<>();
    
    public TradeGroup() {
    		super();
    }
    
    public TradeGroup(long timestamp, TickerGroup tickerGroup) {
    		this.timestamp = timestamp;
    		this.tickerGroup = tickerGroup;
    }

	public long getId() {
		return id;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
    public void addTrade(Trade trade) {
		this.trades.add(trade);
    }

    public List<Trade> getTrades() {
    		return trades;
    }
    
    public TickerGroup getTickerGroup() {
    		return tickerGroup;
    }
    
    public Double getBTCValue() {
    		return trades.stream().mapToDouble(t -> t.getBTCValue()).sum();
    }

	public Map<Currency, Double> getBalances() {
		Map<Currency, Double> holdings = new TreeMap<>();
		
		for (Trade trade : trades) {
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
    
	@Override
	public int compareTo(TradeGroup o) {
		return (int)(timestamp - o.getTimestamp());
	}
}
