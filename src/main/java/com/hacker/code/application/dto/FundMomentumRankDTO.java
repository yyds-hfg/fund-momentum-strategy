package com.hacker.code.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class FundMomentumRankDTO {

    private Integer rank;
    private String fundCode;
    private String fundName;
    private LocalDate navDate;
    private BigDecimal closeNav;
    private BigDecimal shortMomentum;
    private BigDecimal longMomentum;
    private BigDecimal upDaysRatio;
    private BigDecimal momentumScore;
    private List<FundTagDTO> tags = new ArrayList<>();
}
