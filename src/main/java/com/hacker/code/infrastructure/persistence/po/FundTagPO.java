package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fund_tag")
public class FundTagPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagCode;

    private String tagName;

    private String color;

    private LocalDateTime createTime;
}
