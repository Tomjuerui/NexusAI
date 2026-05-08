package com.moyz.nexus.common.mapper;

import com.moyz.nexus.common.entity.UserDayCost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDayCostMapper extends BaseMapper<UserDayCost> {
    Long sumCostByDay(@Param("day") Integer day);

    Long sumCostByDayPeriod(@Param("beginDate") Integer beginDate, @Param("endDate") Integer endDate);
}
