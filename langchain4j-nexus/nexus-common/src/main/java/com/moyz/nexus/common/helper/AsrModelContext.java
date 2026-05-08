package com.moyz.nexus.common.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.enums.ErrorEnum;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.service.SysConfigService;
import com.moyz.nexus.common.languagemodel.AbstractAsrModelService;
import com.moyz.nexus.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatic Speech Recognition (ASR) context class.
 */
@Slf4j
public class AsrModelContext {

    //ModelName to ASR service mapping
    private static final Map<String, AbstractAsrModelService> NAME_TO_SERVICE = new HashMap<>();

    private final AbstractAsrModelService current;

    /**
     * 直接由系统设置来决定使用哪个ASR模型，不需要让用户选择�?
     */
    public AsrModelContext() {
        String asrSetting = SysConfigService.getByKey(NexusConstant.SysConfigKey.ASR_SETTING);
        log.info("asr model setting:{}", asrSetting);
        JsonNode jsonNode = JsonUtil.toJsonNode(asrSetting);
        if (null != jsonNode) {
            String modelName = jsonNode.get("model_name").asText();
            this.current = NAME_TO_SERVICE.get(modelName);
            if (null == this.current) {
                log.error("asr model not found,modelName:{}", modelName);
                throw new BaseException(ErrorEnum.B_ASR_MODEL_NOT_FOUND);
            }
        } else {
            throw new BaseException(ErrorEnum.B_ASR_SETTING_NOT_FOUND);
        }
    }

    public String audioToText(String urlOrUuid) {
        return this.current.audioToText(urlOrUuid);
    }

    public static void addService(AbstractAsrModelService modelService) {
        NAME_TO_SERVICE.put(modelService.getAiModel().getName(), modelService);
    }

    public static void clearByPlatform(String platform) {
        List<String> needDeleted = NAME_TO_SERVICE.values()
                .stream()
                .filter(item -> item.getAiModel().getPlatform().equalsIgnoreCase(platform))
                .map(item -> item.getAiModel().getName())
                .toList();
        for (String key : needDeleted) {
            log.info("delete asr model service,modelName:{}", key);
            NAME_TO_SERVICE.remove(key);
        }
    }
}
