package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fund_momentum_trend")
public class FundMomentumTrendPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String strategyType;
    private String fundCode;
    private LocalDate tradeDate;

    @TableField("slope_7")
    private BigDecimal slope7;

    @TableField("slope_14")
    private BigDecimal slope14;

    @TableField("slope_20")
    private BigDecimal slope20;

    private BigDecimal sigma;
    private String trend;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
