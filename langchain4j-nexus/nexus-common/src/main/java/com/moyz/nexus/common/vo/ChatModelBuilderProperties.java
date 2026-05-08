package com.moyz.nexus.common.vo;

import com.moyz.nexus.common.enums.ErrorEnum;
import com.moyz.nexus.common.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatModelBuilderProperties {
    private Double temperature;

    /**
     * 是否捕获并返回模型的思考内容（�?DeepSeek �?reasoning_content�?
     */
    private Boolean returnThinking;

    /**
     * 获取采样温度，如果温度不合法则获取默认温�?
     */
    public Double getTemperatureWithDefault(double defaultTemperature) {
        if (defaultTemperature < 0 || defaultTemperature > 1) {
            throw new BaseException(ErrorEnum.B_LLM_TEMPERATURE_ERROR);
        }
        if (Objects.isNull(temperature)) {
            return defaultTemperature;
        }
        if (temperature < 0 || temperature > 1) {
            return defaultTemperature;
        }
        return temperature;
    }

}
