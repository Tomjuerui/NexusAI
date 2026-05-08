package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

@Data
@TableName("nexus_knowledge_base_qa")
@Schema(title = "知识库问答记录实�?, description = "知识库问答记录表")
public class KnowledgeBaseQa extends BaseEntity {

    @Schema(title = "uuid")
    @TableField(value = "uuid", jdbcType = JdbcType.VARCHAR)
    private String uuid;

    @Schema(title = "知识库id")
    @TableField("kb_id")
    private Long kbId;

    @Schema(title = "知识库uuid")
    @TableField("kb_uuid")
    private String kbUuid;

    @Schema(title = "来源文档id,以逗号隔开")
    @TableField("source_file_ids")
    private String sourceFileIds;

    @Schema(title = "问题")
    @TableField("question")
    private String question;

    @Schema(title = "最终提供给LLM的提示词")
    @TableField("prompt")
    private String prompt;

    @Schema(title = "提供给LLM的提示词所消耗的token数量")
    @TableField("prompt_tokens")
    private Integer promptTokens;

    @Schema(title = "答案")
    @TableField("answer")
    private String answer;

    @Schema(title = "答案消耗的token")
    @TableField("answer_tokens")
    private Integer answerTokens;

    @Schema(title = "提问用户id")
    @TableField("user_id")
    private Long userId;

    @Schema(title = "nexus_ai_model id")
    @TableField("ai_model_id")
    private Long aiModelId;
}
