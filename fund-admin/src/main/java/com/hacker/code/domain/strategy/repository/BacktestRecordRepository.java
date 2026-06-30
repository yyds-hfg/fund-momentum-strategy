package com.hacker.code.domain.strategy.repository;

import com.hacker.code.infrastructure.persistence.po.BacktestRecordPO;

import java.util.List;
import java.util.Optional;

public interface BacktestRecordRepository {

    void save(BacktestRecordPO record);

    Optional<BacktestRecordPO> findById(Long id);

    List<BacktestRecordPO> findAll();
}
