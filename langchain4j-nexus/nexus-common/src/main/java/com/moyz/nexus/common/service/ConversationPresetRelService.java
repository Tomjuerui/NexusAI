package com.moyz.nexus.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.nexus.common.dto.ConvPresetRelDto;
import com.moyz.nexus.common.entity.ConversationPresetRel;
import com.moyz.nexus.common.mapper.ConversationPresetRelMapper;
import com.moyz.nexus.common.util.MPPageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ConversationPresetRelService extends ServiceImpl<ConversationPresetRelMapper, ConversationPresetRel> {

    public List<ConvPresetRelDto> listByUser(Long userId, Integer limit) {
        List<ConversationPresetRel> list = this.lambdaQuery()
                .eq(ConversationPresetRel::getUserId, userId)
                .eq(ConversationPresetRel::getIsDeleted, false)
                .last("limit " + limit)
                .list();
        return MPPageUtil.convertToList(list, ConvPresetRelDto.class);
    }

    public boolean softDelBy(Long userId, Long convId) {
        return this.lambdaUpdate()
                .eq(ConversationPresetRel::getUserId, userId)
                .eq(ConversationPresetRel::getUserConvId, convId)
                .set(ConversationPresetRel::getIsDeleted, true)
                .update();
    }
}
