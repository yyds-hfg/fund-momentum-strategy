package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("strategy_result")
public class StrategyResultPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate tradeDate;

    private String strategyType;

    private String marketStatus;

    private BigDecimal totalWeight;

    private LocalDateTime createTime;
}
