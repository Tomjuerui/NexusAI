package com.moyz.nexus.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum WfIODataTypeEnum implements BaseEnum {
    TEXT(1, "文本"),
    NUMBER(2, "数字"),
    OPTIONS(3, "下拉选项"),
    FILES(4, "文件列表"),
    BOOL(5, "布尔�?),
    REF_INPUT(6, "引用节点的输入参�?),
    REF_OUTPUT(7, "引用节点的输出参�?);

    private final Integer value;
    private final String desc;

    public static WfIODataTypeEnum getByValue(Integer val) {
        return Arrays.stream(WfIODataTypeEnum.values()).filter(item -> item.value.equals(val)).findFirst().orElse(null);
    }
}
