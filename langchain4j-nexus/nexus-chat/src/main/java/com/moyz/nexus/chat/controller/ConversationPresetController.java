package com.moyz.nexus.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.nexus.common.dto.ConvPresetSearchReq;
import com.moyz.nexus.common.entity.ConversationPreset;
import com.moyz.nexus.common.service.ConversationPresetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/conversation-preset")
@RestController
public class ConversationPresetController {

    @Resource
    private ConversationPresetService conversationPresetService;

    @Operation(summary = "搜索预设会话(角色)")
    @PostMapping("/search")
    public Page<ConversationPreset> page(@RequestBody ConvPresetSearchReq searchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return conversationPresetService.search(searchReq.getTitle(), currentPage, pageSize);
    }
}
