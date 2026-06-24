package com.hacker.code.adapter.web.controller;

import com.hacker.code.application.assembler.StrategyAssembler;
import com.hacker.code.application.dto.RebalanceAdviceDTO;
import com.hacker.code.application.dto.StrategyResultDTO;
import com.hacker.code.application.service.StrategyExecutionAppService;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.shared.util.TradeDateUtil;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.repository.StrategyResultRepository;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/strategy")
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyExecutionAppService strategyExecutionAppService;
    private final StrategyResultRepository strategyResultRepository;
    private final StrategyAssembler strategyAssembler;

    @PostMapping("/execute")
    public RebalanceAdviceDTO execute(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tradeDate) {
        LocalDate date = tradeDate == null ? TradeDateUtil.determineEffectiveTradeDate() : tradeDate;
        RebalanceAdvice advice = strategyExecutionAppService.executeWeeklyStrategy(date);
        return strategyAssembler.toDTO(advice);
    }

    @GetMapping("/latest")
    public List<StrategyResultDTO> latest() {
        return List.of(StrategyType.BALANCED, StrategyType.ACTIVE).stream()
                .map(strategyResultRepository::findLatest)
                .filter(opt -> opt.isPresent())
                .map(opt -> strategyAssembler.toDTO(opt.get()))
                .collect(Collectors.toList());
    }

    @GetMapping("/results")
    public List<StrategyResultDTO> results(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tradeDate) {
        return strategyResultRepository.findByDate(tradeDate).stream()
                .map(strategyAssembler::toDTO)
                .collect(Collectors.toList());
    }
}
