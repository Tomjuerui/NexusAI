package com.moyz.nexus.chat.controller;

import com.aliyun.core.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.entity.AiModel;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.service.AiModelService;
import com.moyz.nexus.common.util.JsonUtil;
import com.moyz.nexus.common.util.LocalCache;
import com.moyz.nexus.common.vo.AsrSetting;
import com.moyz.nexus.common.languagemodel.data.ModelVoice;
import com.moyz.nexus.common.vo.SysConfigResp;
import com.moyz.nexus.common.vo.TtsSetting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.moyz.nexus.common.enums.ErrorEnum.*;

@Slf4j
@RestController
@RequestMapping("/sys/config")
@Validated
public class SysConfigController {

    @Resource
    private AiModelService aiModelService;

    @GetMapping(value = "/public/info")
    public SysConfigResp info() {
        String asrSetting = LocalCache.CONFIGS.get(NexusConstant.SysConfigKey.ASR_SETTING);
        String ttsSetting = LocalCache.CONFIGS.get(NexusConstant.SysConfigKey.TTS_SETTING);
        if (StringUtils.isBlank(asrSetting)) {
            throw new BaseException(B_ASR_SETTING_NOT_FOUND);
        }
        if (StringUtils.isBlank(ttsSetting)) {
            throw new BaseException(B_TTS_SETTING_NOT_FOUND);
        }
        TtsSetting tts = JsonUtil.fromJson(ttsSetting, TtsSetting.class);
        if (null == tts) {
            throw new BaseException(B_TTS_SETTING_NOT_FOUND);
        }

        SysConfigResp sysConfigResp = new SysConfigResp();
        sysConfigResp.setAsrSetting(JsonUtil.fromJson(asrSetting, AsrSetting.class));
        sysConfigResp.setTtsSetting(JsonUtil.fromJson(ttsSetting, TtsSetting.class));

        //Available voices depend on the modelName in ttsSetting
        List<ModelVoice> voices = new ArrayList<>();
        if (tts.getSynthesizerSide().equalsIgnoreCase(NexusConstant.TtsConstant.SYNTHESIZER_SERVER)) {
            AiModel aiModel = aiModelService.getByName(tts.getModelName());
            if (null == aiModel) {
                log.error("Synthesizer side is server, but model {} not found", tts.getModelName());
                throw new BaseException(B_TTS_MODEL_NOT_FOUND);
            }
            JsonNode modelVoices = aiModel.getProperties() == null ? null : aiModel.getProperties().get("voices");
            if (modelVoices == null || !modelVoices.isArray()) {
                log.error("Synthesizer side is server, but voices not found in model {} properties", tts.getModelName());
                throw new BaseException(B_TTS_MODEL_NOT_FOUND);
            }
            for (JsonNode voice : modelVoices) {
                voices.add(JsonUtil.fromJson(voice, ModelVoice.class));
            }
        }
        sysConfigResp.setAvailableVoices(voices);
        return sysConfigResp;
    }
}
