package com.hacker.code.domain.fund.repository;

import com.hacker.code.domain.fund.valueobject.MarketOverview;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketOverviewRepository {

    Optional<MarketOverview> findByTradeDate(LocalDate tradeDate);

    List<MarketOverview> findRecent(LocalDate endDate, int limit);

    void save(MarketOverview overview);

    void saveOrUpdate(MarketOverview overview);

    boolean exists(LocalDate tradeDate);

    boolean isEmpty();

    void createTableIfNotExists();
}
