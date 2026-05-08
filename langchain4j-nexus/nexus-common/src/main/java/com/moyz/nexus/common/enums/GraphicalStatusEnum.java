package com.moyz.nexus.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum GraphicalStatusEnum implements BaseEnum {
    NONE(1, "未图谱化"),
    DOING(2, "正在图谱�?),
    DONE(3, "已图谱化"),
    FAIL(4, "图谱化失�?);

    private final Integer value;
    private final String desc;

    public static GraphicalStatusEnum getByValue(Integer val) {
        return Arrays.stream(GraphicalStatusEnum.values()).filter(item -> item.value.equals(val)).findFirst().orElse(null);
    }
}
