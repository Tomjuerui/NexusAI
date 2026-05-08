package com.moyz.nexus.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("nexus")
@Data
public class NexusProperties {

    private String host;

    private String frontendUrl;

    private String backendUrl;

    private Proxy proxy;

    private String embeddingModel;

    private String vectorDatabase;

    private String graphDatabase;

    private Datasource datasource;

    private Encrypt encrypt;

    @Data
    public static class Proxy {
        private boolean enable;
        private String host;
        private int httpPort;
    }

    @Data
    public static class Datasource {
        private Neo4j neo4j;
    }

    @Data
    public static class Neo4j {
        private String host;
        private int port;
        private String username;
        private String password;
        private String database;
    }

    @Data
    public static class Encrypt {
        private String aesKey;
    }
}
