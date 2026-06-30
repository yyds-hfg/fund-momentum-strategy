package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("backtest_record")
public class BacktestRecordPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate startDate;

    private LocalDate endDate;

    private String strategyTypes;

    private BigDecimal annualReturn;

    private BigDecimal maxDrawdown;

    private BigDecimal sharpeRatio;

    private String detailJson;

    private LocalDateTime createTime;
}
