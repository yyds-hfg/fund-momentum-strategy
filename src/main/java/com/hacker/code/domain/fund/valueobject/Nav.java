package com.hacker.code.domain.fund.valueobject;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class Nav {

    LocalDate date;
    BigDecimal openNav;
    BigDecimal highNav;
    BigDecimal lowNav;
    BigDecimal closeNav;
    Long volume;
}
