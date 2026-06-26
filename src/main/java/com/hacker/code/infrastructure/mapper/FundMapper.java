package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.FundPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FundMapper extends BaseMapper<FundPO> {

    @Select("SELECT f.* FROM fund f " +
            "INNER JOIN fund_tag_relation r ON f.fund_code = r.fund_code " +
            "WHERE r.tag_id = #{tagId} AND f.status = 1")
    List<FundPO> selectByTag(@Param("tagId") Long tagId);

    List<FundPO> selectByCondition(@Param("tagId") Long tagId,
                                   @Param("keyword") String keyword,
                                   @Param("fundType") String fundType,
                                   @Param("status") Integer status);
}
