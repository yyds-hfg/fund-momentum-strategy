package com.hacker.code.application.service;

import com.hacker.code.application.assembler.StrategyAssembler;
import com.hacker.code.application.dto.RebalanceAdviceDTO;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.repository.StrategyResultRepository;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportAppService {

    private final StrategyResultRepository strategyResultRepository;
    private final StrategyAssembler strategyAssembler;
    private final SpringTemplateEngine templateEngine;

    public RebalanceAdvice getLatestWeeklyReport() {
        Optional<StrategyResult> balanced = strategyResultRepository.findLatest(StrategyType.BALANCED);
        Optional<StrategyResult> active = strategyResultRepository.findLatest(StrategyType.ACTIVE);

        LocalDate tradeDate = balanced.map(StrategyResult::getTradeDate)
                .orElseGet(() -> active.map(StrategyResult::getTradeDate).orElse(LocalDate.now()));

        var marketStatus = balanced.map(StrategyResult::getMarketStatus)
                .orElseGet(() -> active.map(StrategyResult::getMarketStatus).orElse(null));

        List<StrategyResult> subResults = new ArrayList<>();
        balanced.ifPresent(subResults::add);
        active.ifPresent(subResults::add);

        return new RebalanceAdvice(tradeDate, marketStatus, subResults);
    }

    public String generateWeeklyReportHtml() {
        RebalanceAdvice advice = getLatestWeeklyReport();
        RebalanceAdviceDTO dto = strategyAssembler.toDTO(advice);

        Context context = new Context();
        context.setVariable("advice", dto);
        return templateEngine.process("weekly-report", context);
    }
}
