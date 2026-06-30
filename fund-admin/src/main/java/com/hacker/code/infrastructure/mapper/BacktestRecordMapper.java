package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.BacktestRecordPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BacktestRecordMapper extends BaseMapper<BacktestRecordPO> {
}
