package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.FundTagRelationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FundTagRelationMapper extends BaseMapper<FundTagRelationPO> {

    int deleteByFundCode(@Param("fundCode") String fundCode);
}
