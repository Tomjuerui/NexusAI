package com.moyz.nexus.common.languagemodel;

import com.alibaba.dashscope.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.nexus.common.entity.AiModel;
import com.moyz.nexus.common.entity.Draw;
import com.moyz.nexus.common.entity.ModelPlatform;
import com.moyz.nexus.common.entity.User;
import com.moyz.nexus.common.enums.ErrorEnum;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.util.JsonUtil;
import com.moyz.nexus.common.languagemodel.wanx.NexusWanxImageModel;
import com.moyz.nexus.common.languagemodel.data.LLMException;
import com.moyz.nexus.common.vo.WanxBackgroundGenerationParams;
import dev.langchain4j.model.image.ImageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 通义万相
 */
@Slf4j
public class DashScopeWanxService extends AbstractImageModelService {

    public DashScopeWanxService(AiModel model, ModelPlatform modelPlatform) {
        super(model, modelPlatform);
    }

    @Override
    public boolean isEnabled() {
        return StringUtils.isNotBlank(platform.getApiKey()) && aiModel.getIsEnable();
    }

    @Override
    protected ImageModel buildImageModel(User user, Draw draw) {
        if (draw.getAiModelName().contains("wanx-background-generation")) {
            JsonNode dynamicParams = draw.getDynamicParams();
            if (dynamicParams.isEmpty()) {
                log.error("动态参数不能为�?);
                throw new BaseException(ErrorEnum.A_PARAMS_ERROR);
            }
            WanxBackgroundGenerationParams dynamicParamsObj = JsonUtil.fromJson(dynamicParams, WanxBackgroundGenerationParams.class);
            if (null == dynamicParamsObj) {
                log.error("动态参数解析失�?);
                throw new BaseException(ErrorEnum.A_PARAMS_ERROR);
            }
            if (StringUtils.isAllBlank(dynamicParamsObj.getRefImageUrl(), dynamicParamsObj.getRefPrompt())) {
                log.error("引导图与提示词不能全部为�?dynamicParams:{}", dynamicParamsObj);
                throw new BaseException(ErrorEnum.A_PARAMS_ERROR);
            }
            NexusWanxImageModel model = NexusWanxImageModel.builder()
                    .baseUrl(platform.getBaseUrl())
                    .apiKey(platform.getApiKey())
                    .modelName(draw.getAiModelName())
                    .task("background-generation")
                    .function("generation")
                    .size(draw.getGenerateSize())
                    .build();
            model.setImageSynthesisParamCustomizer((paramBuilder -> {
                paramBuilder.extraInput("base_image_url", dynamicParamsObj.getBaseImageUrl());
                if (StringUtils.isNotBlank(dynamicParamsObj.getRefImageUrl())) {
                    paramBuilder.extraInput("ref_image_url", dynamicParamsObj.getRefImageUrl());
                }
                if (StringUtils.isNotBlank(dynamicParamsObj.getRefPrompt())) {
                    paramBuilder.extraInput("ref_prompt", dynamicParamsObj.getRefPrompt());
                }
                paramBuilder.parameter("model_version", "v3");
            }));
            return model;
        } else {
            NexusWanxImageModel.WanxImageModelBuilder builder = NexusWanxImageModel.builder()
                    .baseUrl(platform.getBaseUrl())
                    .apiKey(platform.getApiKey())
                    .modelName(draw.getAiModelName())
                    .size(draw.getGenerateSize())
                    .negativePrompt(draw.getNegativePrompt());
            if (null != draw.getGenerateSeed() && draw.getGenerateSeed() > 0) {
                builder.seed(draw.getGenerateSeed());
            }
            return builder.build();
        }

    }

    @Override
    protected LLMException parseError(Object error) {
        if (error instanceof ApiException apiException) {
            LLMException llmException = new LLMException();
            llmException.setType(apiException.getStatus().getCode());
            llmException.setCode(apiException.getStatus().getCode());
            llmException.setMessage(apiException.getStatus().getMessage());
            return llmException;
        }
        return null;
    }
}
