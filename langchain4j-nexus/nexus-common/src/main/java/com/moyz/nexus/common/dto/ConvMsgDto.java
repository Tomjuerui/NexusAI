package com.moyz.nexus.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConvMsgDto {

    @JsonIgnore
    private Long id;

    @Schema(title = "消息的uuid")
    private String uuid;

    @Schema(title = "父级消息id")
    private Long parentMessageId;

    @Schema(title = "对话的消�?)
    private String remark;

    @Schema(title = "思考的内容")
    private String thinkingContent;

    @Schema(title = "音频文件uuid")
    private String audioUuid;

    @Schema(title = "音频文件Url")
    private String audioUrl;

    @Schema(title = "语音聊天时产生的音频时长，单位秒")
    private Integer audioDuration;

    @Schema(title = "产生该消息的角色�?: 用户,2:系统,3:助手")
    private Integer messageRole;

    @Schema(title = "消耗的token数量")
    private Integer tokens;

    @Schema(title = "创建时间")
    private LocalDateTime createTime;

    @Schema(title = "model id")
    private Long aiModelId;

    @Schema(title = "model platform name")
    private String aiModelPlatform;

    @Schema(title = "附件地址")
    private List<String> attachmentUrls;

    @Schema(title = "子级消息（一般指的是AI的响应）")
    private List<ConvMsgDto> children;

    @Schema(title = "内容格式�?：文本；3：音�?)
    private Integer contentType;

    @Schema(title = "是否引用了向量库知识")
    private Boolean isRefEmbedding;

    @Schema(title = "是否引用了图谱库知识")
    private Boolean isRefGraph;

    @Schema(title = "是否引用了记�?)
    private Boolean isRefMemoryEmbedding;
}
