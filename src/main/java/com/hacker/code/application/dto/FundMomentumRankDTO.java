package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class FundMomentumRankDTO {

    private Integer rank;
    private Integer rankChange; // 相对 7 日前排名变化：正数表示上升，负数表示下降
    private String fundCode;
    private String fundName;
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
    private BigDecimal slope7d;         // 7 日斜率
    private BigDecimal slope14d;        // 14 日斜率
    private BigDecimal slope20d;        // 20 日斜率
    private BigDecimal sigma;           // 近期动量分变化标准差

    private List<FundTagDTO> tags = new ArrayList<>();
}
