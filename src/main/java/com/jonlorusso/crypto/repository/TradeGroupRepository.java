package com.jonlorusso.crypto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonlorusso.crypto.entity.TradeGroup;

public interface TradeGroupRepository extends JpaRepository<TradeGroup, Long> {
	TradeGroup findTopByOrderByTimestampDesc();
	TradeGroup findTopByTimestampLessThanOrderByTimestampDesc(long timestamp);

	List<TradeGroup> findTop30ByOrderByTimestampDesc();
}
