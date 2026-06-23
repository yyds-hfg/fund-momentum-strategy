package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.StrategyResultPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StrategyResultMapper extends BaseMapper<StrategyResultPO> {

    @Select("SELECT * FROM strategy_result WHERE strategy_type = #{strategyType} ORDER BY trade_date DESC LIMIT 1")
    StrategyResultPO selectLatest(@Param("strategyType") String strategyType);

    @Select("SELECT * FROM strategy_result WHERE trade_date = #{tradeDate} ORDER BY strategy_type")
    List<StrategyResultPO> selectByDate(@Param("tradeDate") LocalDate tradeDate);

    @Select("SELECT * FROM strategy_result WHERE trade_date BETWEEN #{startDate} AND #{endDate} ORDER BY trade_date, strategy_type")
    List<StrategyResultPO> selectByDateRange(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
