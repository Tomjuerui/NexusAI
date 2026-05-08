package com.moyz.nexus.chat.controller;

import com.moyz.nexus.common.dto.DrawDto;
import com.moyz.nexus.common.dto.DrawListResp;
import com.moyz.nexus.common.service.DrawService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/draw/star")
@Validated
public class DrawStarController {

    @Resource
    private DrawService drawService;

    @GetMapping("/mine")
    public DrawListResp myStars(@RequestParam Long maxId, @RequestParam int pageSize) {
        return drawService.listStarred(maxId, pageSize);
    }

    @Operation(summary = "将绘图任务设置为公开或私�?)
    @PostMapping("/toggle/{uuid}")
    public DrawDto star(@PathVariable @NotBlank String uuid) {
        return drawService.toggleStar(uuid);
    }
}
