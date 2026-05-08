package com.moyz.nexus.common.languagemodel;

import com.moyz.nexus.common.entity.AiModel;
import com.moyz.nexus.common.entity.ModelPlatform;
import com.moyz.nexus.common.enums.ErrorEnum;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.vo.ChatModelBuilderProperties;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static com.moyz.nexus.common.cosntant.NexusConstant.CustomChatRequestParameterKeys.ENABLE_THINKING;

/**
 * DeepSeek API格式兼容OpenAi
 * <p>
 * 1. 根据 returnThinking 配置，设�?OpenAiStreamingChatModel.returnThinking(true) 以捕�?reasoning_content
 * 2. �?ENABLE_THINKING 转换�?DeepSeek 要求�?{"thinking": {"type": "enabled/disabled"}} 格式
 */
@Slf4j
@Accessors(chain = true)
public class DeepSeekLLMService extends OpenAiLLMService {
    public DeepSeekLLMService(AiModel model, ModelPlatform modelPlatform) {
        super(model, modelPlatform);
    }

    @Override
    protected ChatModel doBuildChatModel(ChatModelBuilderProperties properties) {
        if (StringUtils.isBlank(platform.getApiKey())) {
            throw new BaseException(ErrorEnum.B_LLM_SECRET_KEY_NOT_SET);
        }
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .baseUrl(platform.getBaseUrl())
                .modelName(aiModel.getName())
                .temperature(properties.getTemperature())
                .maxRetries(1)
                .timeout(Duration.of(60, ChronoUnit.SECONDS))
                .apiKey(platform.getApiKey());
        if (Boolean.TRUE.equals(properties.getReturnThinking())) {
            builder.returnThinking(true);
        }
        if (StringUtils.isNotBlank(platform.getBaseUrl())) {
            builder.baseUrl(platform.getBaseUrl());
        }
        if (null != proxyAddress && platform.getIsProxyEnable()) {
            HttpClient.Builder httpClientBuilder = HttpClient.newBuilder().proxy(ProxySelector.of(proxyAddress));
            builder.httpClientBuilder(JdkHttpClient.builder().httpClientBuilder(httpClientBuilder));
        }
        return builder.build();
    }

    @Override
    public StreamingChatModel buildStreamingChatModel(ChatModelBuilderProperties properties) {
        if (StringUtils.isBlank(platform.getApiKey())) {
            throw new BaseException(ErrorEnum.B_LLM_SECRET_KEY_NOT_SET);
        }
        double temperature = properties.getTemperatureWithDefault(0.7);
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel
                .builder()
                .baseUrl(platform.getBaseUrl())
                .modelName(aiModel.getName())
                .temperature(temperature)
                .apiKey(platform.getApiKey())
                .timeout(Duration.of(60, ChronoUnit.SECONDS));
        if (Boolean.TRUE.equals(properties.getReturnThinking())) {
            builder.returnThinking(true);
        }
        if (null != proxyAddress && platform.getIsProxyEnable()) {
            HttpClient.Builder httpClientBuilder = HttpClient.newBuilder().proxy(ProxySelector.of(proxyAddress));
            builder.httpClientBuilder(JdkHttpClient.builder().httpClientBuilder(httpClientBuilder));
        }
        return builder.build();
    }

    @Override
    protected ChatRequestParameters doCreateChatRequestParameters(ChatRequestParameters defaultParameters, Map<String, Object> customParameters) {
        Boolean enableThinking = (Boolean) customParameters.remove(ENABLE_THINKING);
        if (null != enableThinking) {
            customParameters.put("thinking", Map.of("type", enableThinking ? "enabled" : "disabled"));
        }
        return super.doCreateChatRequestParameters(defaultParameters, customParameters);
    }
}
