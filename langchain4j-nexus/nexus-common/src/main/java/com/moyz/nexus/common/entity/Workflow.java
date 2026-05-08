package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nexus_workflow")
@Schema(title = "工作流定�?| workflow definition")
public class Workflow extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField("uuid")
    private String uuid;

    @TableField("title")
    private String title;

    @TableField("remark")
    private String remark;

    @TableField("user_id")
    private Long userId;

    @TableField("is_public")
    private Boolean isPublic;

    @TableField("is_enable")
    private Boolean isEnable;
}
