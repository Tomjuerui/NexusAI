package com.moyz.nexus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moyz.nexus.common.entity.WorkflowComponent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkflowComponentMapper extends BaseMapper<WorkflowComponent> {
    Integer countRefNodes(@Param("uuid") String uuid);
}
