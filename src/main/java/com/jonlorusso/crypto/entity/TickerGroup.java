package com.jonlorusso.crypto.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(indexes = @Index(columnList = "timestamp"))
public class TickerGroup {

    @Id
    @GeneratedValue
    private long id;
    private long timestamp;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Ticker> tickers = new ArrayList<>();
    
    public TickerGroup() {
    		super();
    }
    
    public TickerGroup(long timestamp) {
    		this.timestamp = timestamp;
    }

	public long getId() {
		return id;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
    public void addTicker(Ticker ticker) {
		this.tickers.add(ticker);
    }

    public List<Ticker> getTickers() {
    		return tickers;
    }
    
    public boolean isSellSignal() {
		long gainCount = tickers.stream().filter(t -> t.getDayPercentChange() > 0.0).count();
		return gainCount > 1;    	
    }
    
    public Ticker getTicker(Currency currency) {
    		return tickers.stream().filter(t -> t.getCurrency() == currency).findFirst().get();
    }
}
