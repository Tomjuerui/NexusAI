package com.moyz.nexus.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("nexus.dev-mock")
@Data
public class DevMockProperty {
}
