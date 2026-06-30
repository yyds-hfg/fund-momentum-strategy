package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("market_overview")
public class MarketOverviewPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate tradeDate;

    private Long shVolume;
    private BigDecimal shAmount;
    private Long szVolume;
    private BigDecimal szAmount;
    private Long totalVolume;
    private BigDecimal totalAmount;

    private BigDecimal mainInflow;
    private BigDecimal superLargeInflow;
    private BigDecimal largeInflow;
    private BigDecimal mediumInflow;
    private BigDecimal smallInflow;
    private BigDecimal northBoundInflow;

    private BigDecimal shClose;
    private BigDecimal szClose;

    private String source;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
