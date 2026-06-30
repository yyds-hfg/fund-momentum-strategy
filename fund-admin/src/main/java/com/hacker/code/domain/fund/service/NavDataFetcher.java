package com.hacker.code.domain.fund.service;

import com.hacker.code.domain.fund.valueobject.Nav;

import java.time.LocalDate;
import java.util.List;

/**
 * 外部净值数据获取防腐层接口。
 */
public interface NavDataFetcher {

    List<Nav> fetchHistory(String fundCode, LocalDate startDate, LocalDate endDate);

    Nav fetchLatest(String fundCode);
}
