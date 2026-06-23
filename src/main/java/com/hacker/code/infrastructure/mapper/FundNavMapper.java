package com.hacker.code.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hacker.code.infrastructure.persistence.po.FundNavPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FundNavMapper extends BaseMapper<FundNavPO> {

    @Select("SELECT * FROM fund_nav WHERE fund_code = #{fundCode} ORDER BY nav_date DESC LIMIT 1")
    FundNavPO selectLatest(@Param("fundCode") String fundCode);

    @Select("SELECT * FROM fund_nav WHERE fund_code = #{fundCode} AND nav_date BETWEEN #{startDate} AND #{endDate} ORDER BY nav_date ASC")
    List<FundNavPO> selectByDateRange(@Param("fundCode") String fundCode,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
}
