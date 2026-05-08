package com.moyz.nexus.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.nexus.common.config.NexusProperties;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.entity.AiModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class NexusPropertiesUtil {

    public static String EMBEDDING_TABLE_SUFFIX = "";

    private NexusPropertiesUtil() {
    }

    public static Pair<String, Integer> getSuffixAndDimension(NexusProperties NexusProperties) {
        String suffix = "";
        int dimension = 384;
        if (NexusConstant.EmbeddingModel.BGE_SMALL_ZH_V15.equals(NexusProperties.getEmbeddingModel())) {
            dimension = NexusConstant.EmbeddingModel.BGE_SMALL_ZH_V15_DIMENSION;
            suffix = "bge_" + dimension;
            EMBEDDING_TABLE_SUFFIX = suffix;
        }
        //非本地向量模�?
        else if (!NexusConstant.EmbeddingModel.LOCAL_MODELS.contains(NexusProperties.getEmbeddingModel())) {
            AiModel aiModel = getEmbeddingModelByProperty(NexusProperties);
            String platform = aiModel.getPlatform();
            String modelName = aiModel.getName();
            JsonNode jsonNode = aiModel.getProperties().get("dimension");
            if (null == jsonNode) {
                log.error("向量模型找不到定义的维度属�? model id:{}, model name:{}", aiModel.getId(), modelName);
                throw new RuntimeException("model dimension is not configured, model name:" + modelName);
            }
            dimension = jsonNode.asInt();
            suffix = platform + "_" + dimension;
            EMBEDDING_TABLE_SUFFIX = suffix;
        }
        Pair<String, Integer> tableSuffixAndDimension = Pair.of(suffix, dimension);
        log.info("getSuffixAndDimension:{}", tableSuffixAndDimension);
        return tableSuffixAndDimension;
    }

    public static AiModel getEmbeddingModelByProperty(NexusProperties NexusProperties) {
        String[] platformAndModel = NexusProperties.getEmbeddingModel().split(":");
        String platform = platformAndModel[0];
        String modelName = platformAndModel[1];
        AiModel aiModel = LocalCache.MODEL_ID_TO_OBJ.values().stream()
                .filter(item -> item.getPlatform().equals(platform) && item.getName().equals(modelName))
                .findFirst()
                .orElse(null);
        if (null == aiModel) {
            log.error("模型找不到或已被禁用,platform:{},name:{}", platform, modelName);
            throw new RuntimeException("模型找不到或已被禁用 | vector model not found or is disabled,name:" + modelName);
        }
        return aiModel;
    }
}
