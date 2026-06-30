package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fund_nav")
public class FundNavPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fundCode;

    private LocalDate navDate;

    private BigDecimal openNav;

    private BigDecimal highNav;

    private BigDecimal lowNav;

    private BigDecimal closeNav;

    private Long volume;

    private LocalDateTime createTime;
}
