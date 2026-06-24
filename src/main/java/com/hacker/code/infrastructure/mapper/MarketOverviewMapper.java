package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.MarketOverviewPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MarketOverviewMapper extends BaseMapper<MarketOverviewPO> {

    @Select("SELECT * FROM market_overview WHERE trade_date = #{tradeDate} LIMIT 1")
    MarketOverviewPO selectByTradeDate(@Param("tradeDate") LocalDate tradeDate);

    @Select("SELECT * FROM market_overview WHERE trade_date <= #{endDate} ORDER BY trade_date DESC LIMIT #{limit}")
    List<MarketOverviewPO> selectRecent(@Param("endDate") LocalDate endDate, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM market_overview")
    long countAll();

    @Update("CREATE TABLE IF NOT EXISTS market_overview (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "trade_date DATE NOT NULL," +
            "sh_volume BIGINT," +
            "sh_amount DECIMAL(20,4)," +
            "sz_volume BIGINT," +
            "sz_amount DECIMAL(20,4)," +
            "total_volume BIGINT," +
            "total_amount DECIMAL(20,4)," +
            "main_inflow DECIMAL(20,4)," +
            "super_large_inflow DECIMAL(20,4)," +
            "large_inflow DECIMAL(20,4)," +
            "medium_inflow DECIMAL(20,4)," +
            "small_inflow DECIMAL(20,4)," +
            "north_bound_inflow DECIMAL(20,4)," +
            "sh_close DECIMAL(10,4)," +
            "sz_close DECIMAL(10,4)," +
            "source VARCHAR(50)," +
            "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "UNIQUE KEY uk_market_overview_date (trade_date))")
    void createTableIfNotExists();
}
