package com.hacker.code.domain.fund.valueobject;

import lombok.Value;

@Value
public class FundTag {

    Long id;
    String tagCode;
    String tagName;
    String color;
}
