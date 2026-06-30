package com.hacker.code.domain.fund.entity;

import com.hacker.code.domain.fund.valueobject.FundStatus;
import com.hacker.code.domain.fund.valueobject.FundType;
import com.hacker.code.domain.fund.valueobject.Nav;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Fund {

    private String fundCode;
    private String fundName;
    private FundType fundType;
    private String description;
    private LocalDate listedDate;
    private FundStatus status;
    private List<Nav> navHistory = new ArrayList<>();

    public boolean isEnabled() {
        return status == FundStatus.ENABLED;
    }
}
