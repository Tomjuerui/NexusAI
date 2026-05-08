package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.nexus.common.base.JsonNodeTypeHandler;
import com.moyz.nexus.common.base.NodeInputConfigTypeHandler;
import com.moyz.nexus.common.workflow.WfNodeInputConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "nexus_workflow_node", autoResultMap = true)
@Schema(title = "工作流定�?节点 | workflow definition node")
public class WorkflowNode extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField("uuid")
    private String uuid;

    @TableField("workflow_id")
    private Long workflowId;

    @TableField("workflow_component_id")
    private Long workflowComponentId;

    @TableField("title")
    private String title;

    @TableField("remark")
    private String remark;

    @TableField(value = "input_config", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = NodeInputConfigTypeHandler.class)
    private WfNodeInputConfig inputConfig;

    @TableField(value = "node_config", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = JsonNodeTypeHandler.class)
    private ObjectNode nodeConfig;

    @TableField("position_x")
    private Double positionX;

    @TableField("position_y")
    private Double positionY;
}
