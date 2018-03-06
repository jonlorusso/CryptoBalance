package com.jonlorusso.crypto.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(indexes = @Index(columnList = "timestamp"))
public class Ticker {

    @Id
    @GeneratedValue
    private long id;
    
    private long timestamp;
    
    private double priceBTC;
    private double dailyVolume;
    private double dayPercentChange;
    
    @OneToOne
    private Currency currency;
    
    public Ticker() {
    		super();
    }
    
    public Ticker(long timestamp, Currency currency, Double priceBTC, Double dailyVolume, Double dayPercentChange) {
    		this.timestamp = timestamp;
    		this.currency = currency;
        this.priceBTC = priceBTC;
        this.dayPercentChange = dayPercentChange;
    }

	public long getId() {
		return id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Currency getCurrency() {
		return currency;
	}

	public double getPriceBTC() {
		return priceBTC;
	}

	public double getDailyVolume() {
		return dailyVolume;
	}

	public double getDayPercentChange() {
		return dayPercentChange;
	}
}
