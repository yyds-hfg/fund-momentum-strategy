package com.hacker.code.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hacker.code.domain.fund.repository.FundTagRepository;
import com.hacker.code.domain.fund.valueobject.FundTag;
import com.hacker.code.infrastructure.mapper.FundTagMapper;
import com.hacker.code.infrastructure.mapper.FundTagRelationMapper;
import com.hacker.code.infrastructure.persistence.po.FundTagPO;
import com.hacker.code.infrastructure.persistence.po.FundTagRelationPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FundTagRepositoryImpl implements FundTagRepository {

    private final FundTagMapper fundTagMapper;
    private final FundTagRelationMapper relationMapper;

    @Override
    public Optional<FundTag> findById(Long id) {
        return Optional.ofNullable(fundTagMapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<FundTag> findByCode(String tagCode) {
        LambdaQueryWrapper<FundTagPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundTagPO::getTagCode, tagCode);
        return Optional.ofNullable(fundTagMapper.selectOne(wrapper)).map(this::toDomain);
    }

    @Override
    public List<FundTag> findAll() {
        return fundTagMapper.selectList(null).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FundTag> findByFundCode(String fundCode) {
        return fundTagMapper.selectByFundCode(fundCode).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(FundTag tag) {
        fundTagMapper.insert(toPO(tag));
    }

    @Override
    public void update(FundTag tag) {
        LambdaQueryWrapper<FundTagPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundTagPO::getTagCode, tag.getTagCode());
        fundTagMapper.update(toPO(tag), wrapper);
    }

    @Override
    public void deleteById(Long id) {
        fundTagMapper.deleteById(id);
    }

    @Override
    public void bindTag(String fundCode, Long tagId) {
        FundTagRelationPO po = new FundTagRelationPO();
        po.setFundCode(fundCode);
        po.setTagId(tagId);
        relationMapper.insert(po);
    }

    @Override
    public void unbindTag(String fundCode, Long tagId) {
        LambdaQueryWrapper<FundTagRelationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundTagRelationPO::getFundCode, fundCode)
               .eq(FundTagRelationPO::getTagId, tagId);
        relationMapper.delete(wrapper);
    }

    @Override
    public void clearTags(String fundCode) {
        relationMapper.deleteByFundCode(fundCode);
    }

    private FundTag toDomain(FundTagPO po) {
        return new FundTag(po.getId(), po.getTagCode(), po.getTagName(), po.getColor());
    }

    private FundTagPO toPO(FundTag tag) {
        FundTagPO po = new FundTagPO();
        po.setId(tag.getId());
        po.setTagCode(tag.getTagCode());
        po.setTagName(tag.getTagName());
        po.setColor(tag.getColor());
        return po;
    }
}
