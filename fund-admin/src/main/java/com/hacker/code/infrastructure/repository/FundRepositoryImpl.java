package com.hacker.code.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.FundRepository;
import com.hacker.code.domain.fund.valueobject.FundStatus;
import com.hacker.code.domain.fund.valueobject.FundType;
import com.hacker.code.infrastructure.mapper.FundMapper;
import com.hacker.code.infrastructure.persistence.po.FundPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FundRepositoryImpl implements FundRepository {

    private final FundMapper fundMapper;

    @Override
    public Optional<Fund> findByCode(String fundCode) {
        LambdaQueryWrapper<FundPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundPO::getFundCode, fundCode);
        FundPO po = fundMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<Fund> findAllEnabled() {
        LambdaQueryWrapper<FundPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundPO::getStatus, FundStatus.ENABLED.getCode());
        return fundMapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Fund> findAll() {
        return fundMapper.selectList(null).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Fund> findByConditions(String keyword, List<String> fundTypes, boolean includeDisabled) {
        Integer status = includeDisabled ? null : FundStatus.ENABLED.getCode();
        return fundMapper.selectByCondition(keyword, fundTypes, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<Fund> findByConditions(String keyword, List<String> fundTypes, boolean includeDisabled, long page, long size) {
        Integer status = includeDisabled ? null : FundStatus.ENABLED.getCode();
        Page<FundPO> pageParam = new Page<>(page, size);
        IPage<FundPO> poPage = fundMapper.selectByConditionPage(pageParam, keyword, fundTypes, status);
        List<Fund> records = poPage.getRecords().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        Page<Fund> resultPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public void save(Fund fund) {
        fundMapper.insert(toPO(fund));
    }

    @Override
    public void update(Fund fund) {
        LambdaQueryWrapper<FundPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundPO::getFundCode, fund.getFundCode());
        fundMapper.update(toPO(fund), wrapper);
    }

    @Override
    public void deleteByCode(String fundCode) {
        LambdaQueryWrapper<FundPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundPO::getFundCode, fundCode);
        fundMapper.delete(wrapper);
    }

    @Override
    public boolean exists(String fundCode) {
        LambdaQueryWrapper<FundPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundPO::getFundCode, fundCode);
        return fundMapper.selectCount(wrapper) > 0;
    }

    private Fund toDomain(FundPO po) {
        Fund fund = new Fund();
        fund.setFundCode(po.getFundCode());
        fund.setFundName(po.getFundName());
        fund.setFundType(FundType.valueOf(po.getFundType()));
        fund.setDescription(po.getDescription());
        fund.setListedDate(po.getListedDate());
        fund.setStatus(po.getStatus() == FundStatus.ENABLED.getCode() ? FundStatus.ENABLED : FundStatus.DISABLED);
        return fund;
    }

    private FundPO toPO(Fund fund) {
        FundPO po = new FundPO();
        po.setFundCode(fund.getFundCode());
        po.setFundName(fund.getFundName());
        po.setFundType(fund.getFundType().name());
        po.setDescription(fund.getDescription());
        po.setListedDate(fund.getListedDate());
        po.setStatus(fund.getStatus() == FundStatus.ENABLED ? FundStatus.ENABLED.getCode() : FundStatus.DISABLED.getCode());
        return po;
    }
}
