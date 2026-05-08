package com.moyz.nexus.common.vo;

import com.moyz.nexus.common.languagemodel.data.ModelVoice;
import lombok.Data;

import java.util.List;

@Data
public class SysConfigResp {
    private AsrSetting asrSetting;
    private TtsSetting ttsSetting;
    /**
     * ttsSetting中设置的 modelName 对应的可用语音列�?
     */
    private List<ModelVoice> availableVoices;
}
