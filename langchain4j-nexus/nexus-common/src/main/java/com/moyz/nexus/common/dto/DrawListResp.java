package com.moyz.nexus.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class DrawListResp {
    private Long minId;
    private List<DrawDto> draws;
}
