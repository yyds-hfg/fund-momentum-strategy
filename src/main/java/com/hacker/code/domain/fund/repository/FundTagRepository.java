package com.hacker.code.domain.fund.repository;

import com.hacker.code.domain.fund.valueobject.FundTag;

import java.util.List;
import java.util.Optional;

public interface FundTagRepository {

    Optional<FundTag> findById(Long id);

    Optional<FundTag> findByCode(String tagCode);

    List<FundTag> findAll();

    List<FundTag> findByFundCode(String fundCode);

    void save(FundTag tag);

    void update(FundTag tag);

    void deleteById(Long id);

    void bindTag(String fundCode, Long tagId);

    void unbindTag(String fundCode, Long tagId);

    void clearTags(String fundCode);
}
