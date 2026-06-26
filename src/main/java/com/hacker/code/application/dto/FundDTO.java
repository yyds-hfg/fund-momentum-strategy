package com.hacker.code.application.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FundDTO {

    private String fundCode;
    private String fundName;
    private String fundType;
    private String description;
    private LocalDate listedDate;
    private Integer status;
}
