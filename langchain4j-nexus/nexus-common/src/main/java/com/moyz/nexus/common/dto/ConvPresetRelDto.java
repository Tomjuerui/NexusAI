package com.moyz.nexus.common.dto;

import lombok.Data;

@Data
public class ConvPresetRelDto {
    private Long id;
    private String uuid;
    private Long userConvId;
    private Long presetConvId;
}
