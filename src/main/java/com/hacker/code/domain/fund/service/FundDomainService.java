package com.hacker.code.domain.fund.service;

import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.FundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FundDomainService {

    private final FundRepository fundRepository;

    public List<Fund> getCandidatePool() {
        return fundRepository.findAllEnabled();
    }

    public boolean isCandidate(String fundCode) {
        return fundRepository.exists(fundCode);
    }
}
