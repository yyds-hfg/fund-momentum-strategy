package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.FundMomentumTrendPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FundMomentumTrendMapper extends BaseMapper<FundMomentumTrendPO> {

    @Select("SELECT * FROM fund_momentum_trend WHERE strategy_type = #{strategyType} AND trade_date = #{tradeDate}")
    List<FundMomentumTrendPO> selectByStrategyTypeAndTradeDate(@Param("strategyType") String strategyType,
                                                               @Param("tradeDate") LocalDate tradeDate);

    @Select("SELECT * FROM fund_momentum_trend WHERE strategy_type = #{strategyType} AND fund_code = #{fundCode} AND trade_date = #{tradeDate} LIMIT 1")
    FundMomentumTrendPO selectByStrategyTypeAndFundCodeAndTradeDate(@Param("strategyType") String strategyType,
                                                                    @Param("fundCode") String fundCode,
                                                                    @Param("tradeDate") LocalDate tradeDate);
}
