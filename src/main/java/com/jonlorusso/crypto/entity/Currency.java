package com.jonlorusso.crypto.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Currency implements Comparable<Currency> {

    @Id
    @GeneratedValue
    private long id;
    
    @Column(unique = true)
    private String name;
    
    @Column(unique = true)
    private String symbol;
    
    private int rank;
    private double marketCapUSD;
    private double availableSupply;
    private double totalSupply;
    private double maxSupply;
    
    public Currency() {
    		super();
    }
    
    public Currency(String name, String symbol, int rank, Double marketCapUSD, Double availableSupply,  Double totalSupply, Double maxSupply) {
        this.name = name;
        this.symbol = symbol;
        this.rank = rank;
        this.marketCapUSD = marketCapUSD;
        this.availableSupply = availableSupply;
        this.totalSupply = totalSupply;
        this.maxSupply = maxSupply;
    }

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public double getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public double getMarketCapUSD() {
		return marketCapUSD;
	}
	
	public void setMarketCapUSD(double marketCapUSD) {
		this.marketCapUSD = marketCapUSD;
	}

	public double getAvailableSupply() {
		return availableSupply;
	}

	public void setAvailableSupply(double availableSupply) {
		this.availableSupply = availableSupply;
	}

	public double getTotalSupply() {
		return totalSupply;
	}


	public void setTotalSupply(double totalSupply) {
		this.totalSupply = totalSupply;
	}

	public double getMaxSupply() {
		return maxSupply;
	}

	public void setMaxSupply(double maxSupply) {
		this.maxSupply = maxSupply;
	}

	public boolean isBitcoin() {
		return getSymbol().equals("BTC");
	}
	
	@Override
	public int compareTo(Currency o) {
		return symbol.compareTo(o.getSymbol());
	}
}
