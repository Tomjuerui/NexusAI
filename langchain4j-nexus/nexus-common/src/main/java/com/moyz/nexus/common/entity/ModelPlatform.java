package com.moyz.nexus.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "nexus_model_platform", autoResultMap = true)
@Schema(title = "ModelPlatform对象", description = "模型平台�?)
public class ModelPlatform extends BaseEntity {

    @Schema(title = "名称")
    @TableField("name")
    private String name;

    @Schema(title = "标题(更易理解记忆的名�?")
    @TableField("title")
    private String title;

    @Schema(title = "说明")
    @TableField("remark")
    private String remark;

    @Schema(title = "base url")
    @TableField("base_url")
    private String baseUrl;

    @Schema(title = "api key")
    @TableField("api_key")
    private String apiKey;

    @Schema(title = "secret key, 可�?)
    @TableField("secret_key")
    private String secretKey;

    @Schema(title = "是否开启代理，代理的详细配置在全局配置里，路径：nexus.proxy")
    @TableField("is_proxy_enable")
    private Boolean isProxyEnable;

    @Schema(title = "平台接口是否兼容OpenAI API")
    @TableField("is_openai_api_compatible")
    private Boolean isOpenaiApiCompatible;
}
