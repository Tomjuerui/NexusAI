package com.moyz.nexus.common.languagemodel;

import com.moyz.nexus.common.entity.AiModel;
import com.moyz.nexus.common.entity.ModelPlatform;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * API格式兼容OpenAi的各种模型平�?
 */
@Slf4j
@Accessors(chain = true)
public class OpenAiCompatibleLLMService extends OpenAiLLMService {
    public OpenAiCompatibleLLMService(AiModel model, ModelPlatform modelPlatform) {
        super(model, modelPlatform);
    }
}
