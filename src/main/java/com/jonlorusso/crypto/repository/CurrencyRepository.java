package com.jonlorusso.crypto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonlorusso.crypto.entity.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Currency findOneBySymbol(String symbol);
}
