package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.StrategyPositionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StrategyPositionMapper extends BaseMapper<StrategyPositionPO> {

    @Select("SELECT * FROM strategy_position WHERE result_id = #{resultId}")
    List<StrategyPositionPO> selectByResultId(@Param("resultId") Long resultId);
}
