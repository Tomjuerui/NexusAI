package com.moyz.nexus.common.dto;

import com.moyz.nexus.common.dto.mcp.UserMcpCustomizedParam;
import com.moyz.nexus.common.entity.Mcp;
import lombok.Data;

import java.util.List;

@Data
public class UserMcpDto {
    private Long id;

    private String uuid;

    private Long userId;

    private Long mcpId;

    private List<UserMcpCustomizedParam> mcpCustomizedParams;

    private Boolean isEnable;

    private Mcp mcpInfo;
}
