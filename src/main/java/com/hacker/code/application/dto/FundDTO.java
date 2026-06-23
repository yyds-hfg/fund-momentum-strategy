package com.hacker.code.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class FundDTO {

    private String fundCode;
    private String fundName;
    private String fundType;
    private List<FundTagDTO> tags = new ArrayList<>();
    private LocalDate listedDate;
    private Integer status;
}
