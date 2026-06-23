package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.StrategyConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StrategyConfigMapper extends BaseMapper<StrategyConfigPO> {

    @Select("SELECT * FROM strategy_config WHERE strategy_type = #{strategyType} AND status = 1 LIMIT 1")
    StrategyConfigPO selectByType(@Param("strategyType") String strategyType);

    @Select("SELECT * FROM strategy_config WHERE status = 1")
    List<StrategyConfigPO> selectAllEnabled();
}
