package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("nexus_knowledge_base_qa_ref_graph")
@Schema(title = "知识库问答记�?图谱引用", description = "知识库问答记�?图谱引用列表")
public class KnowledgeBaseQaRefGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(title = "问答记录ID")
    @TableField("qa_record_id")
    private Long qaRecordId;

    @Schema(title = "从用户问题中解析出来的实�?)
    @TableField("entities_from_question")
    private String entitiesFromQuestion;

    @Schema(title = "从图数据库中查找得到的图�?)
    @TableField("graph_from_store")
    private String graphFromStore;

    @Schema(title = "提问用户id")
    @TableField("user_id")
    private Long userId;
}
