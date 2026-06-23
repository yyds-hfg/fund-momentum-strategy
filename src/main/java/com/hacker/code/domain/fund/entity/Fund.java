package com.hacker.code.domain.fund.entity;

import com.hacker.code.domain.fund.valueobject.FundStatus;
import com.hacker.code.domain.fund.valueobject.FundTag;
import com.hacker.code.domain.fund.valueobject.FundType;
import com.hacker.code.domain.fund.valueobject.Nav;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Fund {

    private String fundCode;
    private String fundName;
    private FundType fundType;
    private Set<FundTag> tags = new HashSet<>();
    private LocalDate listedDate;
    private FundStatus status;
    private List<Nav> navHistory = new ArrayList<>();

    public boolean isEnabled() {
        return status == FundStatus.ENABLED;
    }

    public void addTag(FundTag tag) {
        this.tags.add(tag);
    }

    public void removeTag(FundTag tag) {
        this.tags.remove(tag);
    }
}
