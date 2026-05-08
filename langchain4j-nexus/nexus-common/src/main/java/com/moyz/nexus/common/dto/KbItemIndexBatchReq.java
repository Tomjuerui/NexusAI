package com.moyz.nexus.common.dto;

import lombok.Data;

@Data
public class KbItemIndexBatchReq {
    private String[] uuids;
    private String[] indexTypes;
}
