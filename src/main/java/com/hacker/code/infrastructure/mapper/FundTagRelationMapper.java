package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.FundTagRelationPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FundTagRelationMapper extends BaseMapper<FundTagRelationPO> {

    @Delete("DELETE FROM fund_tag_relation WHERE fund_code = #{fundCode}")
    int deleteByFundCode(@Param("fundCode") String fundCode);
}
