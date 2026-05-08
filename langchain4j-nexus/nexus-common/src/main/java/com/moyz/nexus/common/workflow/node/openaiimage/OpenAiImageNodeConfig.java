package com.moyz.nexus.common.workflow.node.openaiimage;

import lombok.Data;

@Data
public class OpenAiImageNodeConfig {
    private String prompt;
    private String size;
    private String quality;
}
