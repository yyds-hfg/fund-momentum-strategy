package com.hacker.code.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BacktestDetailDTO {

    private Long id;
    private List<NavPointDTO> navCurve = new ArrayList<>();
    private List<DrawdownPointDTO> drawdownCurve = new ArrayList<>();
}
