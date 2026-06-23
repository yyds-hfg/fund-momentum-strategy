package com.hacker.code.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class SyncResult {

    private LocalDate startDate;
    private LocalDate endDate;
    private int totalFunds;
    private int successFunds;
    private int failedFunds;
    private long totalRecords;
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
    }
}
