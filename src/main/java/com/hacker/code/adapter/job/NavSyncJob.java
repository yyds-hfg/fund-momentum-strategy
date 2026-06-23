package com.hacker.code.adapter.job;

import com.hacker.code.application.service.FundDataSyncAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NavSyncJob {

    private final FundDataSyncAppService fundDataSyncAppService;

    @Scheduled(cron = "0 35 15 ? * MON-FRI")
    public void dailySync() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(6);
        fundDataSyncAppService.syncNavData(startDate, endDate);
    }

}
