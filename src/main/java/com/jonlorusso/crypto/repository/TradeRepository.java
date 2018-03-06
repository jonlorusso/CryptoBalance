package com.jonlorusso.crypto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonlorusso.crypto.entity.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long> {
	
	
}
