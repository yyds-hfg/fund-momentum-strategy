package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FundMomentumRankDTO {

    private Integer rank;
    private Integer rankChange; // 相对 7 日前排名变化：正数表示上升，负数表示下降
    private String fundCode;
    private String fundName;
    private String fundType;
    private String description;
    private LocalDate navDate;
    private BigDecimal closeNav;
    private BigDecimal shortMomentum;
    private BigDecimal longMomentum;
    private BigDecimal upDaysRatio;
    private BigDecimal momentumScore;

    // 动量趋势（预计算）
    private String momentumTrend;       // SHARP_UP / UP / FLAT_UP / FLAT / FLAT_DOWN / DOWN / SHARP_DOWN
    private String momentumTrendLabel;  // 急剧上升 / 上升 等中文标签
    private String momentumTrendDesc;   // 趋势文字描述（tooltip 用）

    // 5/10/20 日均线状态
    private BigDecimal ma5;
    private String ma5Status;   // 跌破 / 站上 / 突破 / 强势突破
    private BigDecimal ma10;
    private String ma10Status;
    private BigDecimal ma20;
    private String ma20Status;
}
