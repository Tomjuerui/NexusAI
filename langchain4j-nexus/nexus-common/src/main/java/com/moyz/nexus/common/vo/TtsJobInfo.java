package com.moyz.nexus.common.vo;

import com.moyz.nexus.common.helper.TtsModelContext;
import lombok.Data;

/**
 * TTS任务中的各种临时数据
 */
@Data
public class TtsJobInfo {
    private String jobId;
    private TtsModelContext ttsModelContext;
    private String filePath;
}
