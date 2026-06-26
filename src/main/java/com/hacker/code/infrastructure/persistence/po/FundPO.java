package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fund")
public class FundPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fundCode;

    private String fundName;

    private String fundType;

    private String description;

    private LocalDate listedDate;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
