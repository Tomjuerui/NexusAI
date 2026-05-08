package com.moyz.nexus.common.vo;

import com.moyz.nexus.common.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SseAskParams {

    private User user;
    //请求标识,�?知识库的记录uuid,搜索记录uuid
    private String uuid;
    private String modelPlatform;
    private String modelName;
    private String regenerateQuestionUuid;
    /**
     * 2:text,3:audio
     */
    private Integer answerContentType;
    private String voice;
    private SseEmitter sseEmitter;
    /**
     * 创建LLM时用到的属性，非必�?
     */
    private ChatModelBuilderProperties modelProperties;

    /**
     * 进行http请求时最终提交给LLM的信息，必填
     */
    private ChatModelRequestParams httpRequestParams;
}
