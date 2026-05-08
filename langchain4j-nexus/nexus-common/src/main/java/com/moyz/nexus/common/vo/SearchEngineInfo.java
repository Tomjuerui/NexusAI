package com.moyz.nexus.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moyz.nexus.common.interfaces.AbstractSearchEngineService;
import lombok.Data;

@Data
public class SearchEngineInfo {
    private String name;
    private Boolean enable;
    @JsonIgnore
    private AbstractSearchEngineService searchEngineService;
}
