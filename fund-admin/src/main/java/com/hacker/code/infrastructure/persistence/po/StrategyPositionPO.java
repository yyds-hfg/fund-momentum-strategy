package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("strategy_position")
public class StrategyPositionPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resultId;

    private String fundCode;

    private String fundName;

    private BigDecimal weight;

    private String sourceStrategy;

    private String reason;
}
