package com.hacker.code.application.assembler;

import com.hacker.code.application.dto.FundDTO;
import com.hacker.code.domain.fund.entity.Fund;
import org.springframework.stereotype.Component;

@Component
public class FundAssembler {

    public FundDTO toDTO(Fund fund) {
        FundDTO dto = new FundDTO();
        dto.setFundCode(fund.getFundCode());
        dto.setFundName(fund.getFundName());
        dto.setFundType(fund.getFundType().name());
        dto.setDescription(fund.getDescription());
        dto.setListedDate(fund.getListedDate());
        dto.setStatus(fund.getStatus() == com.hacker.code.domain.fund.valueobject.FundStatus.ENABLED ? 1 : 0);
        return dto;
    }
}
