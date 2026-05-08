package com.moyz.nexus.common.languagemodel;

import com.moyz.nexus.common.entity.AiModel;
import com.moyz.nexus.common.entity.ModelPlatform;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;

public class CommonModelService {
    protected InetSocketAddress proxyAddress;
    @Getter
    protected AiModel aiModel;
    @Setter
    @Getter
    protected ModelPlatform platform;

    public CommonModelService(AiModel aiModel, ModelPlatform modelPlatform) {
        this.aiModel = aiModel;
        this.platform = modelPlatform;

        //兼容旧版配置部分没有 api_key 的情况，后续统一使用 api_key 字段名作为秘钥字�?
        if (null != platform && StringUtils.isNotBlank(platform.getSecretKey()) && StringUtils.isBlank(platform.getApiKey())) {
            platform.setApiKey(platform.getSecretKey());
        }
    }

}
