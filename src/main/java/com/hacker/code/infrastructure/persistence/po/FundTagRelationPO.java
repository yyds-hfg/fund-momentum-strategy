package com.hacker.code.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("fund_tag_relation")
public class FundTagRelationPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fundCode;

    private Long tagId;
}
