package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.moyz.nexus.common.config.UserMcpSettingTypeHandler;
import com.moyz.nexus.common.dto.mcp.UserMcpCustomizedParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "nexus_user_mcp", autoResultMap = true)
@Schema(title = "用户MCP实体")
public class UserMcp extends BaseEntity {

    @Schema(title = "uuid")
    @TableField(value = "uuid")
    private String uuid;

    @Schema(title = "user_id")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(title = "mcp_id")
    @TableField(value = "mcp_id")
    private Long mcpId;

    @Schema(title = "用户的参数设�?)
    @TableField(value = "mcp_customized_params", jdbcType = JdbcType.ARRAY, typeHandler = UserMcpSettingTypeHandler.class)
    private List<UserMcpCustomizedParam> mcpCustomizedParams;

    @Schema(title = "是否启用")
    @TableField(value = "is_enable")
    private Boolean isEnable;
}
