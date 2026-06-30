package com.hacker.code.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hacker.code.domain.fund.repository.MarketOverviewRepository;
import com.hacker.code.domain.fund.valueobject.MarketOverview;
import com.hacker.code.infrastructure.mapper.MarketOverviewMapper;
import com.hacker.code.infrastructure.persistence.po.MarketOverviewPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MarketOverviewRepositoryImpl implements MarketOverviewRepository {

    private final MarketOverviewMapper marketOverviewMapper;

    @Override
    public Optional<MarketOverview> findByTradeDate(LocalDate tradeDate) {
        return Optional.ofNullable(marketOverviewMapper.selectByTradeDate(tradeDate)).map(this::toDomain);
    }

    @Override
    public List<MarketOverview> findRecent(LocalDate endDate, int limit) {
        return marketOverviewMapper.selectRecent(endDate, limit).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(MarketOverview overview) {
        marketOverviewMapper.insert(toPO(overview));
    }

    @Override
    public void saveOrUpdate(MarketOverview overview) {
        MarketOverviewPO existing = marketOverviewMapper.selectByTradeDate(overview.getTradeDate());
        MarketOverviewPO po = toPO(overview);
        if (existing != null) {
            po.setId(existing.getId());
            marketOverviewMapper.updateById(po);
        } else {
            marketOverviewMapper.insert(po);
        }
    }

    @Override
    public boolean exists(LocalDate tradeDate) {
        LambdaQueryWrapper<MarketOverviewPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketOverviewPO::getTradeDate, tradeDate);
        return marketOverviewMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isEmpty() {
        return marketOverviewMapper.countAll() == 0;
    }

    @Override
    public void createTableIfNotExists() {
        marketOverviewMapper.createTableIfNotExists();
    }

    private MarketOverview toDomain(MarketOverviewPO po) {
        return MarketOverview.builder()
                .tradeDate(po.getTradeDate())
                .shVolume(po.getShVolume())
                .shAmount(po.getShAmount())
                .szVolume(po.getSzVolume())
                .szAmount(po.getSzAmount())
                .totalVolume(po.getTotalVolume())
                .totalAmount(po.getTotalAmount())
                .mainInflow(po.getMainInflow())
                .superLargeInflow(po.getSuperLargeInflow())
                .largeInflow(po.getLargeInflow())
                .mediumInflow(po.getMediumInflow())
                .smallInflow(po.getSmallInflow())
                .northBoundInflow(po.getNorthBoundInflow())
                .shClose(po.getShClose())
                .szClose(po.getSzClose())
                .source(po.getSource())
                .build();
    }

    private MarketOverviewPO toPO(MarketOverview domain) {
        MarketOverviewPO po = new MarketOverviewPO();
        po.setTradeDate(domain.getTradeDate());
        po.setShVolume(domain.getShVolume());
        po.setShAmount(domain.getShAmount());
        po.setSzVolume(domain.getSzVolume());
        po.setSzAmount(domain.getSzAmount());
        po.setTotalVolume(domain.getTotalVolume());
        po.setTotalAmount(domain.getTotalAmount());
        po.setMainInflow(domain.getMainInflow());
        po.setSuperLargeInflow(domain.getSuperLargeInflow());
        po.setLargeInflow(domain.getLargeInflow());
        po.setMediumInflow(domain.getMediumInflow());
        po.setSmallInflow(domain.getSmallInflow());
        po.setNorthBoundInflow(domain.getNorthBoundInflow());
        po.setShClose(domain.getShClose());
        po.setSzClose(domain.getSzClose());
        po.setSource(domain.getSource());
        return po;
    }
}
