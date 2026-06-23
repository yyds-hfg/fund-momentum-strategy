package com.hacker.code.application.assembler;

import com.hacker.code.application.dto.FundDTO;
import com.hacker.code.application.dto.FundTagDTO;
import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.valueobject.FundTag;
import org.springframework.stereotype.Component;

@Component
public class FundAssembler {

    public FundDTO toDTO(Fund fund) {
        FundDTO dto = new FundDTO();
        dto.setFundCode(fund.getFundCode());
        dto.setFundName(fund.getFundName());
        dto.setFundType(fund.getFundType().name());
        dto.setListedDate(fund.getListedDate());
        dto.setStatus(fund.getStatus() == com.hacker.code.domain.fund.valueobject.FundStatus.ENABLED ? 1 : 0);
        for (FundTag tag : fund.getTags()) {
            dto.getTags().add(toDTO(tag));
        }
        return dto;
    }

    public FundTagDTO toDTO(FundTag tag) {
        FundTagDTO dto = new FundTagDTO();
        dto.setId(tag.getId());
        dto.setTagCode(tag.getTagCode());
        dto.setTagName(tag.getTagName());
        dto.setColor(tag.getColor());
        return dto;
    }
}
