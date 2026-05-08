package com.moyz.nexus.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SearchEngineResp {
    private List<SearchReturnWebPage> items;
}
