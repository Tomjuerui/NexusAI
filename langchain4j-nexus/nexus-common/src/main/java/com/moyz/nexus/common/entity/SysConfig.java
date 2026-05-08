package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("nexus_sys_config")
@Schema(title = "系统配置�?)
public class SysConfig extends BaseEntity {

    @Schema(title = "配置名称")
    @TableField("name")
    private String name;

    @Schema(title = "配置项的�?)
    private String value;

}
