package com.hacker.code.adapter.job;

import com.hacker.code.application.service.StrategyExecutionAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class StrategyJob implements CommandLineRunner {

    private final StrategyExecutionAppService strategyExecutionAppService;

    @Scheduled(cron = "0 35 15 ? * FRI")
    public void weeklyExecute() {
        strategyExecutionAppService.executeWeeklyStrategy(LocalDate.now());
    }

    @Override
    public void run(String... args) throws Exception {
        weeklyExecute();
    }

}
