package com.moyz.nexus.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moyz.nexus.common.entity.ConversationMessageRefGraph;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationMessageRefGraphMapper extends BaseMapper<ConversationMessageRefGraph> {
    List<ConversationMessageRefGraph> listByMsgUuid(@Param("uuid") String uuid);
}
