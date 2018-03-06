package com.jonlorusso.crypto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonlorusso.crypto.entity.TickerGroup;

public interface TickerGroupRepository extends JpaRepository<TickerGroup, Long> {
	TickerGroup findTopByOrderByTimestampDesc();
	TickerGroup findTopByTimestampLessThanOrderByTimestampDesc(long timestamp);
	List<TickerGroup> findTop100ByOrderByTimestampDesc();
}
