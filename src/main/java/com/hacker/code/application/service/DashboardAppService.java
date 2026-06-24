package com.hacker.code.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.application.assembler.FundAssembler;
import com.hacker.code.application.assembler.StrategyAssembler;
import com.hacker.code.application.dto.*;
import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.service.FundDomainService;
import com.hacker.code.domain.fund.valueobject.FundTag;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.portfolio.valueobject.RebalanceAdvice;
import com.hacker.code.domain.shared.util.TradeDateUtil;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.repository.BacktestRecordRepository;
import com.hacker.code.domain.strategy.repository.StrategyConfigRepository;
import com.hacker.code.domain.strategy.service.MomentumCalculator;
import com.hacker.code.domain.strategy.service.UpQualityCalculator;
import com.hacker.code.domain.strategy.valueobject.Momentum;
import com.hacker.code.domain.strategy.valueobject.UpQuality;
import com.hacker.code.infrastructure.persistence.po.BacktestRecordPO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAppService {

    private final ReportAppService reportAppService;
    private final StrategyAssembler strategyAssembler;
    private final FundAssembler fundAssembler;
    private final BacktestRecordRepository backtestRecordRepository;
    private final ObjectMapper objectMapper;
    private final FundDomainService fundDomainService;
    private final NavDataRepository navDataRepository;
    private final StrategyConfigRepository strategyConfigRepository;
    private final MomentumCalculator momentumCalculator;
    private final UpQualityCalculator upQualityCalculator;

    public DashboardDTO getDashboardData() {
        RebalanceAdvice advice = reportAppService.getLatestWeeklyReport();
        RebalanceAdviceDTO dto = strategyAssembler.toDTO(advice);

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setTradeDate(dto.getTradeDate());
        dashboard.setMarketStatus(dto.getMarketStatus());
        dashboard.setPositions(dto.getSubResults().stream()
                .flatMap(r -> r.getPositions().stream())
                .collect(Collectors.toList()));
        dashboard.setMomentumRankGroups(getMomentumRankGroups(TradeDateUtil.determineEffectiveTradeDate()));
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

    public List<FundMomentumRanksGroupDTO> getMomentumRankGroups(LocalDate tradeDate) {
        return buildMomentumRankGroups(tradeDate, 10);
    }

    public List<FundMomentumRanksGroupDTO> getAllMomentumRankGroups(LocalDate tradeDate) {
        return buildMomentumRankGroups(tradeDate, Integer.MAX_VALUE);
    }

    public List<FundMomentumRanksGroupDTO> getAllMomentumRankGroupsForLatestTradeDate() {
        return getAllMomentumRankGroups(TradeDateUtil.determineEffectiveTradeDate());
    }

    private List<FundMomentumRanksGroupDTO> buildMomentumRankGroups(LocalDate tradeDate, int limit) {
        List<Fund> candidates = fundDomainService.getCandidatePool().stream()
                .filter(fund -> !"000852".equals(fund.getFundCode()))
                .collect(Collectors.toList());

        LocalDate targetDate = tradeDate;

        List<FundMomentumRanksGroupDTO> groups = new ArrayList<>();
        for (StrategyConfig config : strategyConfigRepository.findAllEnabled()) {
            List<FundMomentumRankDTO> ranks = calculateMomentumRanks(candidates, config, targetDate, limit);
            FundMomentumRanksGroupDTO group = new FundMomentumRanksGroupDTO();
            group.setStrategyType(config.getStrategyType().name());
            group.setStrategyName(config.getStrategyType().getDescription());
            group.setRanks(ranks);
            groups.add(group);
        }
        return groups;
    }

    private List<FundMomentumRankDTO> calculateMomentumRanks(List<Fund> candidates, StrategyConfig config, LocalDate targetDate, int limit) {
        int maxWindow = Math.max(config.getLongMomentumWindow(),
                Math.max(config.getMaWindow(), config.getVolatilityWindow()));
        LocalDate queryStartDate = targetDate.minusDays(maxWindow + 30);

        // 找到每个基金在目标日期及之前最新的净值日期，取最小值作为统一参考日
        List<LocalDate> latestDates = new ArrayList<>();
        for (Fund fund : candidates) {
            List<Nav> history = navDataRepository.findByDateRange(fund.getFundCode(), queryStartDate, targetDate);
            if (!history.isEmpty()) {
                latestDates.add(history.get(history.size() - 1).getDate());
            }
        }
        if (latestDates.isEmpty()) {
            return List.of();
        }
        LocalDate referenceDate = latestDates.stream()
                .min(Comparator.naturalOrder())
                .orElse(targetDate);

        List<FundMomentumRankDTO> ranks = new ArrayList<>();
        for (Fund fund : candidates) {
            List<Nav> navHistory = navDataRepository.findByDateRange(fund.getFundCode(), queryStartDate, targetDate);
            if (navHistory.size() < config.getLongMomentumWindow() + 1) {
                continue;
            }
            // 截取到统一参考日
            List<Nav> trimmed = trimToDate(navHistory, referenceDate);
            if (trimmed.isEmpty() || trimmed.size() < config.getLongMomentumWindow() + 1) {
                continue;
            }
            Nav latest = trimmed.get(trimmed.size() - 1);

            Momentum shortMomentum = momentumCalculator.calculate(trimmed, config.getShortMomentumWindow());
            Momentum longMomentum = momentumCalculator.calculate(trimmed, config.getLongMomentumWindow());
            UpQuality upQuality = upQualityCalculator.calculate(trimmed, config.getLongMomentumWindow());

            FundMomentumRankDTO rank = new FundMomentumRankDTO();
            rank.setFundCode(fund.getFundCode());
            rank.setFundName(fund.getFundName());
            rank.setNavDate(latest.getDate());
            rank.setCloseNav(latest.getCloseNav());
            BigDecimal momentumScore = calculateMomentumScore(
                    shortMomentum.getValue(), longMomentum.getValue(), upQuality.getUpDaysRatio());
            rank.setShortMomentum(shortMomentum.getValue());
            rank.setLongMomentum(longMomentum.getValue());
            rank.setUpDaysRatio(upQuality.getUpDaysRatio());
            rank.setMomentumScore(momentumScore);
            for (FundTag tag : fund.getTags()) {
                rank.getTags().add(fundAssembler.toDTO(tag));
            }
            ranks.add(rank);
        }

        ranks.sort(Comparator.comparing(FundMomentumRankDTO::getMomentumScore).reversed());
        List<FundMomentumRankDTO> result = new ArrayList<>();
        int resultSize = Math.min(limit, ranks.size());
        for (int i = 0; i < resultSize; i++) {
            FundMomentumRankDTO rank = ranks.get(i);
            rank.setRank(i + 1);
            result.add(rank);
        }
        return result;
    }

    private BigDecimal calculateMomentumScore(BigDecimal shortMomentum,
                                              BigDecimal longMomentum,
                                              BigDecimal upDaysRatio) {
        // 动量分 = 长期动量*0.6 + 短期动量*0.3 + 上涨占比*100*0.1
        return longMomentum.multiply(new BigDecimal("0.6"))
                .add(shortMomentum.multiply(new BigDecimal("0.3")))
                .add(upDaysRatio.multiply(BigDecimal.valueOf(100)).multiply(new BigDecimal("0.1")))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private List<Nav> trimToDate(List<Nav> navHistory, LocalDate referenceDate) {
        int index = -1;
        for (int i = 0; i < navHistory.size(); i++) {
            if (!navHistory.get(i).getDate().isAfter(referenceDate)) {
                index = i;
            } else {
                break;
            }
        }
        if (index < 0) {
            return List.of();
        }
        return new ArrayList<>(navHistory.subList(0, index + 1));
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
