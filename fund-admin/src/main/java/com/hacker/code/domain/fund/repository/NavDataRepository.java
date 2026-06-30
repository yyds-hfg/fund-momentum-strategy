package com.hacker.code.domain.fund.repository;

import com.hacker.code.domain.fund.valueobject.Nav;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NavDataRepository {

    Optional<Nav> findLatest(String fundCode);

    List<Nav> findByDateRange(String fundCode, LocalDate startDate, LocalDate endDate);

    void save(String fundCode, Nav nav);

    void batchSave(String fundCode, List<Nav> navList);

    boolean exists(String fundCode, LocalDate navDate);
}
