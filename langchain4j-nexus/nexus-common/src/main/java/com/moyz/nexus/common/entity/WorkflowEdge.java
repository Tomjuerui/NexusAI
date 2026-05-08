package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nexus_workflow_edge")
@Schema(title = "工作流定�?�?| workflow definition edge")
public class WorkflowEdge extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField("uuid")
    private String uuid;

    @TableField("workflow_id")
    private Long workflowId;

    @TableField("source_node_uuid")
    private String sourceNodeUuid;

    @TableField("source_handle")
    private String sourceHandle;

    @TableField("target_node_uuid")
    private String targetNodeUuid;
}
