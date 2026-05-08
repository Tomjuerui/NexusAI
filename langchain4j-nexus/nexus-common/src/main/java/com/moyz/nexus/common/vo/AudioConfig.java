package com.moyz.nexus.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioConfig {

    private Voice voice;

    //SampleRate、PitchRate ...

    @Data
    public static class Voice {

        /**
         * 用于API请求的音色参数名�?
         */
        @JsonProperty("param_name")
        private String paramName;

        private String model;
        private String platform;
    }
}
