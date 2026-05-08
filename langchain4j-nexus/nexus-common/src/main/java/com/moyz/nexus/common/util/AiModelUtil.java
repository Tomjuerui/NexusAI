package com.moyz.nexus.common.util;

import com.moyz.nexus.common.cosntant.NexusConstant;
import org.apache.commons.lang3.StringUtils;

public class AiModelUtil {

    private AiModelUtil() {
    }

    public static boolean checkModelType(String modelType) {
        return NexusConstant.ModelType.getModelType().contains(modelType);
    }

    public static boolean checkModelPlatform(String platform) {
        return NexusConstant.ModelPlatform.getModelConstants().contains(platform);
    }
}
