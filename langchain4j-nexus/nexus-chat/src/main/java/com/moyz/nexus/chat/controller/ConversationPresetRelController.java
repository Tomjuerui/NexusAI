package com.moyz.nexus.chat.controller;

import com.moyz.nexus.common.base.ThreadContext;
import com.moyz.nexus.common.dto.ConvPresetRelDto;
import com.moyz.nexus.common.service.ConversationPresetRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "预设对话与用户对话关联关系controller")
@RequestMapping("/conversation-preset-rel")
@RestController
public class ConversationPresetRelController {

    @Resource
    private ConversationPresetRelService conversationPresetRelService;

    @Operation(summary = "获取当前用户使用到的预设会话")
    @GetMapping("/mine")
    public List<ConvPresetRelDto> mine(@Parameter(description = "限制数量") @RequestParam(defaultValue = "100") Integer limit) {
        return conversationPresetRelService.listByUser(ThreadContext.getCurrentUserId(), limit);
    }
}
