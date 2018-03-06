package com.jonlorusso.crypto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonlorusso.crypto.entity.Ticker;

public interface TickerRepository extends JpaRepository<Ticker, Long> {
    List<Ticker> findByTimestamp(long timestamp);
    List<Ticker> findByTimestampBetween(long beginTimestamp, long endTimestamp);
}
