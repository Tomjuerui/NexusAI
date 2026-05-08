package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.nexus.common.base.ObjectNodeTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "nexus_ai_model", autoResultMap = true)
@Schema(title = "AiModel对象", description = "AI模型�?)
public class AiModel extends BaseEntity {

    @Schema(title = "模型类型:text,image,embedding,rerank")
    @TableField("type")
    private String type;

    @Schema(title = "模型名称")
    @TableField("name")
    private String name;

    @Schema(title = "模型标题(更易理解记忆的名�?")
    @TableField("title")
    private String title;

    @Schema(title = "模型所属平�?)
    @TableField("platform")
    private String platform;

    @Schema(title = "模型配置")
    @TableField("setting")
    private String setting;

    @Schema(title = "说明")
    @TableField("remark")
    private String remark;

    @Schema(title = "是否免费(true:免费,false:收费)")
    @TableField("is_free")
    private Boolean isFree;

    @Schema(title = "状�?1:正常使用,0:不可�?")
    @TableField("is_enable")
    private Boolean isEnable;

    @Schema(title = "上下文长�?)
    @TableField("context_window")
    private Integer contextWindow;

    @Schema(title = "最大输入长�?)
    @TableField("max_input_tokens")
    private Integer maxInputTokens;

    @Schema(title = "最大输出长�?)
    @TableField("max_output_tokens")
    private Integer maxOutputTokens;

    @Schema(title = "输入类型")
    @TableField("input_types")
    private String inputTypes;

    @Schema(title = "支持的输出格�? text,json_object")
    @TableField("response_format_types")
    private String responseFormatTypes;

    @Schema(title = "属�?)
    @TableField(value = "properties", jdbcType = JdbcType.JAVA_OBJECT, typeHandler = ObjectNodeTypeHandler.class)
    private ObjectNode properties;

    @Schema(title = "是否推理模型")
    @TableField("is_reasoner")
    private Boolean isReasoner;

    @Schema(title = "思考过程是否可以关�?)
    @TableField("is_thinking_closable")
    private Boolean isThinkingClosable;
    
    @Schema(title = "是否支持web搜索")
    @TableField("is_support_web_search")
    private Boolean isSupportWebSearch;
}
