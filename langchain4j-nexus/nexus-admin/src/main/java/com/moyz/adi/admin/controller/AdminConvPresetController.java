package com.moyz.nexus.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.nexus.common.dto.ConvPresetAddReq;
import com.moyz.nexus.common.dto.ConvPresetEditReq;
import com.moyz.nexus.common.dto.ConvPresetSearchReq;
import com.moyz.nexus.common.entity.ConversationPreset;
import com.moyz.nexus.common.service.ConversationPresetService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/conv-preset")
@Validated
public class AdminConvPresetController {

    @Resource
    private ConversationPresetService conversationPresetService;

    @PostMapping("/search")
    public Page<ConversationPreset> page(@RequestBody ConvPresetSearchReq keyword, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return conversationPresetService.search(keyword.getTitle(), currentPage, pageSize);
    }

    @PostMapping("/addOne")
    public ConversationPreset addOne(@RequestBody ConvPresetAddReq presetAddReq) {
        return conversationPresetService.addOne(presetAddReq);
    }

    @PostMapping("/edit/{uuid}")
    public boolean edit(@PathVariable String uuid, @RequestBody ConvPresetEditReq editReq) {
        return conversationPresetService.edit(uuid, editReq);
    }

    @PostMapping("/del/{uuid}")
    public void delete(@PathVariable String uuid) {
        conversationPresetService.softDel(uuid);
    }
}
