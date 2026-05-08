package com.moyz.nexus.common.dto;

import com.moyz.nexus.common.annotation.NotAllFieldsEmptyCheck;
import lombok.Data;

@Data
@NotAllFieldsEmptyCheck
public class UserUpdateReq {
    private String secretKey;
    private Boolean contextEnable;
    private Integer contextMsgPairNum;
}
