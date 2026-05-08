package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.moyz.nexus.common.enums.EmbeddingStatusEnum;
import com.moyz.nexus.common.enums.GraphicalStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("nexus_knowledge_base_item")
@Schema(title = "知识库条目实�?, description = "知识库条目表")
public class KnowledgeBaseItem extends BaseEntity {

    @Schema(title = "知识库id")
    @TableField("kb_id")
    private Long kbId;

    @Schema(title = "知识库uuid")
    @TableField("kb_uuid")
    private String kbUuid;

    @Schema(title = "名称")
    @TableField("source_file_id")
    private Long sourceFileId;

    @Schema(title = "uuid")
    @TableField("uuid")
    private String uuid;

    @Schema(title = "标题")
    @TableField("title")
    private String title;

    @Schema(title = "内容摘要")
    @TableField("brief")
    private String brief;

    @Schema(title = "内容")
    @TableField("remark")
    private String remark;

    @Schema(title = "向量化状�?)
    @TableField("embedding_status")
    private EmbeddingStatusEnum embeddingStatus;

    @Schema(title = "向量化状态变更时间点")
    @TableField("embedding_status_change_time")
    private LocalDateTime embeddingStatusChangeTime;

    @Schema(title = "图谱化状�?)
    @TableField("graphical_status")
    private GraphicalStatusEnum graphicalStatus;

    @Schema(title = "图谱化状态变更时间点")
    @TableField("graphical_status_change_time")
    private LocalDateTime graphicalStatusChangeTime;
}
