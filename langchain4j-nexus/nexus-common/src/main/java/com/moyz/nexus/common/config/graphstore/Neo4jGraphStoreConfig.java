package com.moyz.nexus.common.config.graphstore;

import com.moyz.nexus.common.config.NexusProperties;
import com.moyz.nexus.common.rag.GraphStore;
import com.moyz.nexus.common.rag.neo4j.Neo4jGraphStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "nexus.graph-database", havingValue = "neo4j")
public class Neo4jGraphStoreConfig {

    @Resource
    private NexusProperties NexusProperties;

    @Bean(name = "kbGraphStore")
    @Primary
    public GraphStore initGraphStore() {
        log.info("neo4j graph store init");
        NexusProperties.Neo4j neo4j = NexusProperties.getDatasource().getNeo4j();
        return Neo4jGraphStore
                .builder()
                .host(neo4j.getHost())
                .port(neo4j.getPort())
                .user(neo4j.getUsername())
                .password(neo4j.getPassword())
                .graphName("nexus_knowledge_base_graph")
                .dropGraphFirst(false)
                .build();
    }
}
