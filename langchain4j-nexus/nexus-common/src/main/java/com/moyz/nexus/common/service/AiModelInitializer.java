package com.moyz.nexus.common.service;

import com.google.common.base.Joiner;
import com.moyz.nexus.common.config.NexusProperties;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.entity.AiModel;
import com.moyz.nexus.common.entity.ModelPlatform;
import com.moyz.nexus.common.enums.ErrorEnum;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.helper.AsrModelContext;
import com.moyz.nexus.common.helper.ImageModelContext;
import com.moyz.nexus.common.helper.LLMContext;
import com.moyz.nexus.common.helper.TtsModelContext;
import com.moyz.nexus.common.languagemodel.*;
import com.moyz.nexus.common.searchengine.GoogleSearchEngineService;
import com.moyz.nexus.common.searchengine.SearchEngineServiceContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.moyz.nexus.common.cosntant.NexusConstant.ModelType.*;
import static com.moyz.nexus.common.util.LocalCache.MODEL_ID_TO_OBJ;

@Slf4j
@Service
public class AiModelInitializer {

    @Resource
    private NexusProperties NexusProperties;

    @Resource
    private ModelPlatformService modelPlatformService;

    private InetSocketAddress proxyAddress;

    private List<AiModel> all = new ArrayList<>();

    /**
     * 模型及其配置初始�?
     *
     * @param allModels
     */
    public void init(List<AiModel> allModels) {
        this.all = allModels;
        for (AiModel model : all) {
            if (Boolean.TRUE.equals(model.getIsEnable())) {
                MODEL_ID_TO_OBJ.put(model.getId(), model);
            } else {
                MODEL_ID_TO_OBJ.remove(model.getId());
            }
        }
        if (NexusProperties.getProxy().isEnable()) {
            proxyAddress = new InetSocketAddress(NexusProperties.getProxy().getHost(), NexusProperties.getProxy().getHttpPort());
        } else {
            proxyAddress = null;
        }

        Map<String, ModelPlatform> nameToPlatform = modelPlatformService.listAll().stream().collect(Collectors.toMap(ModelPlatform::getName, Function.identity(), (v1, v2) -> v1));
        initLLMServiceList(nameToPlatform, TEXT);
        initLLMServiceList(nameToPlatform, VISION);
        initImageModelServiceList(nameToPlatform);
        initAsrModelServiceList(nameToPlatform);
        initTtsModelServiceList(nameToPlatform);
    }

    /**
     * 初始化大语言模型列表
     *
     * @param nameToPlatform 模型平台名称与模型平台详情映�?
     * @param modelType      模型类型：text | vision
     */
    private synchronized void initLLMServiceList(Map<String, ModelPlatform> nameToPlatform, String modelType) {

        // OpenAi api 兼容模型
        initOpenAiCompatibleService(nameToPlatform, (model, modelPlatformName) -> new OpenAiCompatibleLLMService(model, nameToPlatform.get(modelPlatformName)).setProxyAddress(proxyAddress));

        //deepseek
        initLLMService(NexusConstant.ModelPlatform.DEEPSEEK, modelType, model -> new DeepSeekLLMService(model, nameToPlatform.get(NexusConstant.ModelPlatform.DEEPSEEK)).setProxyAddress(proxyAddress));

        //openai
        initLLMService(NexusConstant.ModelPlatform.OPENAI, modelType, model -> new OpenAiLLMService(model, nameToPlatform.get(NexusConstant.ModelPlatform.OPENAI)).setProxyAddress(proxyAddress));

        //dashscope
        initLLMService(NexusConstant.ModelPlatform.DASHSCOPE, modelType, model -> new DashScopeLLMService(model, nameToPlatform.get(NexusConstant.ModelPlatform.DASHSCOPE)).setProxyAddress(proxyAddress));

        //ollama
        initLLMService(NexusConstant.ModelPlatform.OLLAMA, modelType, model -> new OllamaLLMService(model, nameToPlatform.get(NexusConstant.ModelPlatform.OLLAMA)));

        // 硅基流动
        initLLMService(NexusConstant.ModelPlatform.SILICONFLOW, modelType, model -> new SiliconflowLLMService(model, nameToPlatform.get(NexusConstant.ModelPlatform.SILICONFLOW)));
    }

    /**
     * 初始化图片服务、搜索服�?
     *
     * @param nameToPlatform 模型平台名称与模型平台详情映�?
     */
    private synchronized void initImageModelServiceList(Map<String, ModelPlatform> nameToPlatform) {

        initImageModelService(NexusConstant.ModelPlatform.OPENAI, model -> new OpenAiImageService(model, nameToPlatform.get(NexusConstant.ModelPlatform.OPENAI)).setProxyAddress(proxyAddress));
        initImageModelService(NexusConstant.ModelPlatform.DASHSCOPE, model -> new DashScopeWanxService(model, nameToPlatform.get(NexusConstant.ModelPlatform.DASHSCOPE)));
        initImageModelService(NexusConstant.ModelPlatform.SILICONFLOW, model -> new SiliconflowImageModelService(model, nameToPlatform.get(NexusConstant.ModelPlatform.SILICONFLOW)));

        //search engine
        SearchEngineServiceContext.addWebSearcher(NexusConstant.SearchEngineName.GOOGLE, new GoogleSearchEngineService(proxyAddress));
    }

    /**
     * 初始化语音识别服�?
     *
     * @param nameToPlatform 模型平台名称与模型平台详情映�?
     */
    private synchronized void initAsrModelServiceList(Map<String, ModelPlatform> nameToPlatform) {
        initAsrModelService(NexusConstant.ModelPlatform.DASHSCOPE, model -> new DashScopeAsrService(model, nameToPlatform.get(NexusConstant.ModelPlatform.DASHSCOPE)));
        initAsrModelService(NexusConstant.ModelPlatform.SILICONFLOW, model -> new SiliconflowAsrService(model, nameToPlatform.get(NexusConstant.ModelPlatform.SILICONFLOW)));
    }

    /**
     * 初始化语音合成服�?
     *
     * @param nameToPlatform 模型平台名称与模型平台详情映�?
     */
    private synchronized void initTtsModelServiceList(Map<String, ModelPlatform> nameToPlatform) {
        initTtsModelService(NexusConstant.ModelPlatform.DASHSCOPE, model -> new DashScopeTtsService(model, nameToPlatform.get(NexusConstant.ModelPlatform.DASHSCOPE)));
        initTtsModelService(NexusConstant.ModelPlatform.SILICONFLOW, model -> new SiliconflowTtsService(model, nameToPlatform.get(NexusConstant.ModelPlatform.SILICONFLOW)));
    }

    private void initLLMService(String platform, String modelType, Function<AiModel, AbstractLLMService> function) {
        List<AiModel> models = all.stream().filter(item -> item.getType().equals(modelType) && item.getPlatform().equals(platform)).toList();
        if (CollectionUtils.isEmpty(models)) {
            log.warn("{} service is disabled", platform);
        }
        LLMContext.clearByPlatform(platform, modelType);
        for (AiModel model : models) {
            log.info("add llm model,model:{}", model);
            LLMContext.addLLMService(function.apply(model));
        }
    }

    private void initOpenAiCompatibleService(Map<String, ModelPlatform> nameToPlatform, BiFunction<AiModel, String, AbstractLLMService> function) {
        log.info("init openai api compatible llm model");
        List<String> compatiblePlatforms = nameToPlatform.values().stream().filter(ModelPlatform::getIsOpenaiApiCompatible).map(ModelPlatform::getName).toList();
        for (String platform : compatiblePlatforms) {
            List<AiModel> models = all.stream().filter(item -> item.getType().equals(TEXT) && item.getPlatform().equals(platform)).toList();
            if (CollectionUtils.isEmpty(models)) {
                log.warn("{} service is disabled", Joiner.on(",").join(compatiblePlatforms));
            }
            for (AiModel model : models) {
                log.info("add openai api compatible llm model,model:{}", model);
                LLMContext.addLLMService(function.apply(model, platform));
            }
        }
    }

    private void initImageModelService(String platform, Function<AiModel, AbstractImageModelService> function) {
        List<AiModel> models = all.stream().filter(item -> item.getType().equals(IMAGE) && item.getPlatform().equals(platform)).toList();
        if (CollectionUtils.isEmpty(models)) {
            log.warn("{} service is disabled", platform);
        }
        ImageModelContext.clearByPlatform(platform);
        for (AiModel model : models) {
            log.info("add image model,model:{}", model);
            ImageModelContext.addImageModelService(function.apply(model));
        }

    }

    private void initAsrModelService(String platform, Function<AiModel, AbstractAsrModelService> function) {
        List<AiModel> models = all.stream().filter(item -> item.getType().equals(NexusConstant.ModelType.ASR) && item.getPlatform().equals(platform)).toList();
        if (CollectionUtils.isEmpty(models)) {
            log.warn("{} service is disabled", platform);
        }
        AsrModelContext.clearByPlatform(platform);
        for (AiModel model : models) {
            log.info("add asr model,model:{}", model);
            AsrModelContext.addService(function.apply(model));
        }
    }

    private void initTtsModelService(String platform, Function<AiModel, AbstractTtsModelService> function) {
        List<AiModel> models = all.stream().filter(item -> item.getType().equals(NexusConstant.ModelType.TTS) && item.getPlatform().equals(platform)).toList();
        if (CollectionUtils.isEmpty(models)) {
            log.warn("{} service is disabled", platform);
        }
        TtsModelContext.clearByPlatform(platform);
        for (AiModel model : models) {
            log.info("add tts model,model:{}", model);
            TtsModelContext.addService(function.apply(model));
        }
    }


    public void delete(AiModel aiModel) {
        LLMContext.remove(aiModel.getPlatform(), aiModel.getName());
        ImageModelContext.remove(aiModel.getName());
        MODEL_ID_TO_OBJ.remove(aiModel.getId());
    }

    public void addOrUpdate(AiModel aiModel) {
        AiModel existOne = all.stream().filter(item -> item.getId().equals(aiModel.getId())).findFirst().orElse(null);
        if (null == existOne) {
            all.add(aiModel);
        } else {
            BeanUtils.copyProperties(aiModel, existOne);
        }
        Map<String, ModelPlatform> nameToPlatform = modelPlatformService.listAll().stream().collect(Collectors.toMap(ModelPlatform::getName, Function.identity(), (v1, v2) -> v1));
        String modelType = aiModel.getType();
        if (TEXT.equalsIgnoreCase(modelType) || VISION.equalsIgnoreCase(modelType)) {
            initLLMServiceList(nameToPlatform, aiModel.getType());
        } else if (IMAGE.equalsIgnoreCase(modelType)) {
            initImageModelServiceList(nameToPlatform);
        } else if (ASR.equalsIgnoreCase(modelType)) {
            initAsrModelServiceList(nameToPlatform);
        } else {
            throw new BaseException(ErrorEnum.A_MODEL_NOT_FOUND);
        }
        MODEL_ID_TO_OBJ.put(aiModel.getId(), aiModel);
    }
}
