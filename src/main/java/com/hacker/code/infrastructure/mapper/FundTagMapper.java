package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.FundTagPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FundTagMapper extends BaseMapper<FundTagPO> {

    @Select("SELECT t.* FROM fund_tag t " +
            "INNER JOIN fund_tag_relation r ON t.id = r.tag_id " +
            "WHERE r.fund_code = #{fundCode}")
    List<FundTagPO> selectByFundCode(@Param("fundCode") String fundCode);
}
