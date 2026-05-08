package com.moyz.nexus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moyz.nexus.common.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * <p>
 * 会话�?Mapper 接口
 * </p>
 *
 * @author moyz
 * @since 2023-04-11
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    Integer countCreatedByTimePeriod(@Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

    Integer countAllCreated();
}
