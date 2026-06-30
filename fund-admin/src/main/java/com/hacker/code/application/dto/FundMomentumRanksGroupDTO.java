package com.hacker.code.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FundMomentumRanksGroupDTO {

    private String strategyType;
    private String strategyName;
    private List<FundMomentumRankDTO> ranks = new ArrayList<>();
}
