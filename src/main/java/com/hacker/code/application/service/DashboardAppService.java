package com.hacker.code.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.application.assembler.StrategyAssembler;
import com.hacker.code.application.dto.*;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.repository.BacktestRecordRepository;
import com.hacker.code.infrastructure.persistence.po.BacktestRecordPO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAppService {

    private final ReportAppService reportAppService;
    private final StrategyAssembler strategyAssembler;
    private final BacktestRecordRepository backtestRecordRepository;
    private final ObjectMapper objectMapper;

    public DashboardDTO getDashboardData() {
        RebalanceAdvice advice = reportAppService.getLatestWeeklyReport();
        RebalanceAdviceDTO dto = strategyAssembler.toDTO(advice);

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setTradeDate(dto.getTradeDate());
        dashboard.setMarketStatus(dto.getMarketStatus());
        dashboard.setPositions(dto.getSubResults().stream()
                .flatMap(r -> r.getPositions().stream())
                .collect(Collectors.toList()));
        dashboard.setBacktestRecords(getBacktestRecords());
        return dashboard;
    }

    public List<BacktestRecordDTO> getBacktestRecords() {
        return backtestRecordRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public BacktestDetailDTO getBacktestDetail(Long id) {
        BacktestRecordPO po = backtestRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Backtest not found: " + id));

        List<Map<String, Object>> records = objectMapper.readValue(po.getDetailJson(), new TypeReference<>() {});
        BacktestDetailDTO detail = new BacktestDetailDTO();
        detail.setId(id);

        for (Map<String, Object> record : records) {
            String date = (String) record.get("date");
            BigDecimal nav = new BigDecimal(record.get("nav").toString());
            BigDecimal drawdown = new BigDecimal(record.get("drawdown").toString());
            detail.getNavCurve().add(new NavPointDTO(date, nav));
            detail.getDrawdownCurve().add(new DrawdownPointDTO(date, drawdown.multiply(new BigDecimal("100"))));
        }
        return detail;
    }

    private BacktestRecordDTO toDTO(BacktestRecordPO po) {
        BacktestRecordDTO dto = new BacktestRecordDTO();
        dto.setId(po.getId());
        dto.setStartDate(po.getStartDate());
        dto.setEndDate(po.getEndDate());
        dto.setAnnualReturn(po.getAnnualReturn());
        dto.setMaxDrawdown(po.getMaxDrawdown());
        dto.setSharpeRatio(po.getSharpeRatio());
        return dto;
    }
}
