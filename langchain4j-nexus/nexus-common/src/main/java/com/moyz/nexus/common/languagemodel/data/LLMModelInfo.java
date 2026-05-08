package com.moyz.nexus.common.languagemodel.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moyz.nexus.common.languagemodel.AbstractLLMService;
import lombok.Data;

@Data
public class LLMModelInfo extends ModelInfo {

    @JsonIgnore
    private AbstractLLMService llmService;
}
