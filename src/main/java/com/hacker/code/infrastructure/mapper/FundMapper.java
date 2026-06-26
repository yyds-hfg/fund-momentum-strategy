package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.FundPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FundMapper extends BaseMapper<FundPO> {

    List<FundPO> selectByCondition(@Param("keyword") String keyword,
                                   @Param("fundTypes") List<String> fundTypes,
                                   @Param("status") Integer status);
}
