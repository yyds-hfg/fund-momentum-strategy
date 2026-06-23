package com.hacker.code.infrastructure.repository;

import com.hacker.code.domain.strategy.repository.BacktestRecordRepository;
import com.hacker.code.infrastructure.mapper.BacktestRecordMapper;
import com.hacker.code.infrastructure.persistence.po.BacktestRecordPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BacktestRecordRepositoryImpl implements BacktestRecordRepository {

    private final BacktestRecordMapper backtestRecordMapper;

    @Override
    public void save(BacktestRecordPO record) {
        backtestRecordMapper.insert(record);
    }

    @Override
    public Optional<BacktestRecordPO> findById(Long id) {
        return Optional.ofNullable(backtestRecordMapper.selectById(id));
    }

    @Override
    public List<BacktestRecordPO> findAll() {
        return backtestRecordMapper.selectList(null);
    }
}
