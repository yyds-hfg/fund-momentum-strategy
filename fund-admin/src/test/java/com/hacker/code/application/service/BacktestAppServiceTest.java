package com.hacker.code.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.strategy.entity.StrategyResult;
import com.hacker.code.domain.strategy.repository.BacktestRecordRepository;
import com.hacker.code.domain.strategy.valueobject.MarketStatus;
import com.hacker.code.domain.strategy.valueobject.StrategyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BacktestAppServiceTest {

    @Mock
    private StrategyExecutionAppService strategyExecutionAppService;

    @Mock
    private NavDataRepository navDataRepository;

    @Mock
    private BacktestRecordRepository backtestRecordRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BacktestAppService backtestAppService;

    @Test
    void shouldRunBacktestWithTwoPeriods() throws Exception {
        LocalDate d1 = LocalDate.of(2024, 1, 5);  // Friday
        LocalDate d2 = LocalDate.of(2024, 1, 12); // Friday
        LocalDate d3 = LocalDate.of(2024, 1, 19); // Friday

        StrategyResult result = new StrategyResult();
        result.setTradeDate(d1);
        result.setStrategyType(StrategyType.BALANCED);
        result.setMarketStatus(MarketStatus.STRONG);
        result.setTotalWeight(new BigDecimal("0.7"));
        result.addPosition(new Position("510300", "沪深300ETF", new BigDecimal("0.7"), StrategyType.BALANCED, "reason"));

        RebalanceAdvice advice = new RebalanceAdvice(d1, MarketStatus.STRONG, List.of(result),
                List.of(new Position("510300", "沪深300ETF", new BigDecimal("0.7"), StrategyType.MERGED, "merged reason")));

        when(strategyExecutionAppService.executeWeeklyStrategy(any())).thenReturn(advice);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // 510300 价格：d1=100, d2=105, d3=110
        when(navDataRepository.findByDateRange("510300", d1.minusDays(10), d1))
                .thenReturn(List.of(nav(d1, "100")));
        when(navDataRepository.findByDateRange("510300", d2.minusDays(10), d2))
                .thenReturn(List.of(nav(d2, "105")));
        when(navDataRepository.findByDateRange("510300", d3.minusDays(10), d3))
                .thenReturn(List.of(nav(d3, "110")));

        var response = backtestAppService.runBacktest(d1, d3);

        assertNotNull(response);
        ArgumentCaptor<com.hacker.code.infrastructure.persistence.po.BacktestRecordPO> captor =
                ArgumentCaptor.forClass(com.hacker.code.infrastructure.persistence.po.BacktestRecordPO.class);
        verify(backtestRecordRepository, times(1)).save(captor.capture());
        assertEquals(d1, captor.getValue().getStartDate());
        assertEquals(d3, captor.getValue().getEndDate());
    }

    private Nav nav(LocalDate date, String close) {
        return new Nav(date, new BigDecimal(close), new BigDecimal(close), new BigDecimal(close), new BigDecimal(close), 0L);
    }
}
