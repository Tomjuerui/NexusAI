package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("nexus_conversation_preset_rel")
@Schema(title = "预设对话与用户会话关系实�?, description = "预设对话与用户会话关系表")
public class ConversationPresetRel extends BaseEntity {
    private String uuid;
    private Long userId;
    private Long presetConvId;
    private Long userConvId;
}
