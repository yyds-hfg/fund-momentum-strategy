package com.hacker.code.domain.fund.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hacker.code.domain.fund.entity.Fund;

import java.util.List;
import java.util.Optional;

public interface FundRepository {

    Optional<Fund> findByCode(String fundCode);

    List<Fund> findAllEnabled();

    List<Fund> findAll();

    List<Fund> findByConditions(String keyword, List<String> fundTypes, boolean includeDisabled);

    IPage<Fund> findByConditions(String keyword, List<String> fundTypes, boolean includeDisabled, long page, long size);

    void save(Fund fund);

    void update(Fund fund);

    void deleteByCode(String fundCode);

    boolean exists(String fundCode);
}
