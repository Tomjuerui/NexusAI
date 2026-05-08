package com.moyz.nexus.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum EmbeddingStatusEnum implements BaseEnum {
    NONE(1, "未向量化"),
    DOING(2, "正在向量�?),
    DONE(3, "已向量化"),
    FAIL(4, "向量化失�?);

    private final Integer value;
    private final String desc;

    public static EmbeddingStatusEnum getByValue(Integer val) {
        return Arrays.stream(EmbeddingStatusEnum.values()).filter(item -> item.value.equals(val)).findFirst().orElse(null);
    }
}
