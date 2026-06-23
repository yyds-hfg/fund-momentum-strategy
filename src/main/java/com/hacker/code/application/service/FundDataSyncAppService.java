package com.hacker.code.application.service;

import com.hacker.code.application.dto.SyncResult;
import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.FundRepository;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.service.NavDataFetcher;
import com.hacker.code.domain.fund.valueobject.Nav;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 基金数据同步应用服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundDataSyncAppService {

    private final FundRepository fundRepository;
    private final NavDataRepository navDataRepository;
    private final NavDataFetcher navDataFetcher;

    public SyncResult syncNavData(LocalDate startDate, LocalDate endDate) {
        SyncResult result = new SyncResult();
        result.setStartDate(startDate);
        result.setEndDate(endDate);

        List<Fund> funds = fundRepository.findAllEnabled();
        result.setTotalFunds(funds.size());

        for (Fund fund : funds) {
            try {
                int count = syncSingleFund(fund.getFundCode(), startDate, endDate);
                if (count > 0) {
                    result.setSuccessFunds(result.getSuccessFunds() + 1);
                    result.setTotalRecords(result.getTotalRecords() + count);
                } else {
                    result.setFailedFunds(result.getFailedFunds() + 1);
                    result.addError(fund.getFundCode() + ": 未获取到数据");
                }
            } catch (Exception e) {
                result.setFailedFunds(result.getFailedFunds() + 1);
                result.addError(fund.getFundCode() + ": " + e.getMessage());
                log.error("Sync NAV failed for {}", fund.getFundCode(), e);
            }
        }

        log.info("NAV sync completed. Total: {}, Success: {}, Failed: {}, Records: {}",
                result.getTotalFunds(), result.getSuccessFunds(), result.getFailedFunds(), result.getTotalRecords());
        return result;
    }

    public SyncResult syncNavDataForFund(String fundCode, LocalDate startDate, LocalDate endDate) {
        SyncResult result = new SyncResult();
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setTotalFunds(1);

        int count = syncSingleFund(fundCode, startDate, endDate);
        if (count > 0) {
            result.setSuccessFunds(1);
            result.setTotalRecords(count);
        } else {
            result.setFailedFunds(1);
            result.addError(fundCode + ": 未获取到数据");
        }
        return result;
    }

    private int syncSingleFund(String fundCode, LocalDate startDate, LocalDate endDate) {
        List<Nav> navList = navDataFetcher.fetchHistory(fundCode, startDate, endDate);
        int count = 0;
        for (Nav nav : navList) {
            if (!navDataRepository.exists(fundCode, nav.getDate())) {
                navDataRepository.save(fundCode, nav);
                count++;
            }
        }
        log.info("Synced {} records for {}", count, fundCode);
        return count;
    }

    public void validateNavCoverage(LocalDate tradeDate) {
        List<Fund> funds = fundRepository.findAllEnabled();
        for (Fund fund : funds) {
            boolean exists = navDataRepository.exists(fund.getFundCode(), tradeDate);
            if (!exists) {
                log.warn("NAV missing for {} on {}", fund.getFundCode(), tradeDate);
            }
        }
    }
}
