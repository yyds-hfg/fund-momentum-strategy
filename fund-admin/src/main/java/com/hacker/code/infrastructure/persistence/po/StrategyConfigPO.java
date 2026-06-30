package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("strategy_config")
public class StrategyConfigPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String strategyType;

    private Integer shortMomentumWindow;

    private Integer longMomentumWindow;

    private BigDecimal upDaysThreshold;

    private Integer maWindow;

    private Integer volatilityWindow;

    private Integer maxHoldingCount;

    private BigDecimal singleWeightCap;

    private String rebalancingFrequency;

    private Integer coolingPeriodDays;

    private BigDecimal allocationRatio;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
