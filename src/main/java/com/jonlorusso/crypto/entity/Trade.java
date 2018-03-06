package com.jonlorusso.crypto.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(indexes = @Index(columnList = "timestamp"))
public class Trade implements Comparable<Trade> {

    @Id
    @GeneratedValue
    private long id;
    
    private long timestamp;
    
    @OneToOne
    private Currency baseCurrency;
    
    @OneToOne
    private Currency quoteCurrency;
    
    private Double price;
    private Double quantity;
    
    public static enum TradeType { BUY, SELL };
    
    private TradeType tradeType;
    
    public Trade() {
    		super();
    }
    
    public Trade(long timestamp, Currency baseCurrency, Currency quoteCurrency, Double price, Double quantity, TradeType tradeType) {
    		this.timestamp = timestamp;
    		this.baseCurrency = baseCurrency;
    		this.quoteCurrency = quoteCurrency;
    		this.price = price;
    		this.quantity = quantity;
    		this.tradeType = tradeType;
    }

	public long getId() {
		return id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Currency getBaseCurrency() {
		return baseCurrency;
	}

	public Currency getQuoteCurrency() {
		return quoteCurrency;
	}

	public Double getPrice() {
		return price;
	}

	public Double getQuantity() {
		return quantity;
	}

	public TradeType getTradeType() {
		return tradeType;
	}
	
	public Double getBTCValue() {
		return quantity * price;
	}

	@Override
	public int compareTo(Trade o) {
		return baseCurrency.getSymbol().compareTo(o.getBaseCurrency().getSymbol());
	}
}
