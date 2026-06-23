package com.hacker.code.application.service;

import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.FundRepository;
import com.hacker.code.domain.fund.repository.FundTagRepository;
import com.hacker.code.domain.fund.valueobject.FundStatus;
import com.hacker.code.domain.fund.valueobject.FundTag;
import com.hacker.code.domain.fund.valueobject.FundType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundAppService {

    private final FundRepository fundRepository;
    private final FundTagRepository fundTagRepository;

    @Transactional
    public void addFund(String fundCode, String fundName, String fundTypeName, List<Long> tagIds) {
        Fund fund = new Fund();
        fund.setFundCode(fundCode);
        fund.setFundName(fundName);
        fund.setFundType(FundType.valueOf(fundTypeName));
        fund.setStatus(FundStatus.ENABLED);
        fundRepository.save(fund);
        bindTags(fundCode, tagIds);
    }

    @Transactional
    public void updateFund(String fundCode, String fundName, String fundTypeName, Integer status, List<Long> tagIds) {
        Fund fund = fundRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("Fund not found: " + fundCode));
        fund.setFundName(fundName);
        fund.setFundType(FundType.valueOf(fundTypeName));
        fund.setStatus(status == 1 ? FundStatus.ENABLED : FundStatus.DISABLED);
        fundRepository.update(fund);
        fundTagRepository.clearTags(fundCode);
        bindTags(fundCode, tagIds);
    }

    @Transactional
    public void deleteFund(String fundCode) {
        fundTagRepository.clearTags(fundCode);
        fundRepository.deleteByCode(fundCode);
    }

    @Transactional
    public void bindTags(String fundCode, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        Set<Long> existing = fundTagRepository.findByFundCode(fundCode).stream()
                .map(FundTag::getId)
                .collect(Collectors.toSet());
        for (Long tagId : tagIds) {
            if (!existing.contains(tagId)) {
                fundTagRepository.bindTag(fundCode, tagId);
            }
        }
    }

    public Fund getFund(String fundCode) {
        return fundRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("Fund not found: " + fundCode));
    }
}
