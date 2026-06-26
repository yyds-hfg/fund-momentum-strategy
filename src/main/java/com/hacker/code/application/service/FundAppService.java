package com.hacker.code.application.service;

import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.FundRepository;
import com.hacker.code.domain.fund.valueobject.FundStatus;
import com.hacker.code.domain.fund.valueobject.FundType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundAppService {

    private final FundRepository fundRepository;

    @Transactional
    public void addFund(String fundCode, String fundName, String fundTypeName, String description) {
        Fund fund = new Fund();
        fund.setFundCode(fundCode);
        fund.setFundName(fundName);
        fund.setFundType(FundType.valueOf(fundTypeName));
        fund.setDescription(description);
        fund.setStatus(FundStatus.ENABLED);
        fundRepository.save(fund);
    }

    @Transactional
    public void updateFund(String fundCode, String fundName, String fundTypeName, String description, Integer status) {
        Fund fund = fundRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("Fund not found: " + fundCode));
        fund.setFundName(fundName);
        fund.setFundType(FundType.valueOf(fundTypeName));
        fund.setDescription(description);
        fund.setStatus(status == 1 ? FundStatus.ENABLED : FundStatus.DISABLED);
        fundRepository.update(fund);
    }

    @Transactional
    public void deleteFund(String fundCode) {
        fundRepository.deleteByCode(fundCode);
    }

    public Fund getFund(String fundCode) {
        return fundRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("Fund not found: " + fundCode));
    }
}
