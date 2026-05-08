package com.moyz.nexus.common.config.embeddingstore;

import com.moyz.nexus.common.config.NexusProperties;
import com.moyz.nexus.common.util.NexusPropertiesUtil;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * pgvector的相关配�?
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "nexus.vector-database", havingValue = "pgvector")
public class PgVectorEmbeddingStoreConfig {

    @Value("${spring.datasource.url}")
    private String dataBaseUrl;

    @Value("${spring.datasource.username}")
    private String dataBaseUserName;

    @Value("${spring.datasource.password}")
    private String dataBasePassword;

    @Resource
    private NexusProperties NexusProperties;

    @Bean(name = "kbEmbeddingStore")
    @Primary
    public EmbeddingStore<TextSegment> initKbEmbeddingStore() {
        log.info("Initializing kbEmbeddingStore...");
        String tableName = "nexus_knowledge_base_embedding";
        Pair<String, Integer> pair = NexusPropertiesUtil.getSuffixAndDimension(NexusProperties);
        if (StringUtils.isNotBlank(pair.getLeft())) {
            tableName = tableName + "_" + pair.getLeft();
        }
        return createEmbeddingStore(tableName, pair.getRight());
    }

    /**
     * 角色记忆使用的向量库
     *
     * @return EmbeddingStore实例
     */
    @Bean(name = "convMemoryEmbeddingStore")
    public EmbeddingStore<TextSegment> initConvMemoryEmbeddingStore() {
        log.info("Initializing convMemoryEmbeddingStore...");
        String tableName = "nexus_conversation_memory_embedding";
        Pair<String, Integer> pair = NexusPropertiesUtil.getSuffixAndDimension(NexusProperties);
        if (StringUtils.isNotBlank(pair.getLeft())) {
            tableName = tableName + "_" + pair.getLeft();
        }
        return createEmbeddingStore(tableName, pair.getRight());
    }

    @Bean(name = "searchEmbeddingStore")
    public EmbeddingStore<TextSegment> initSearchEmbeddingStore() {
        log.info("Initializing searchEmbeddingStore...");
        String tableName = "nexus_ai_search_embedding";
        Pair<String, Integer> pair = NexusPropertiesUtil.getSuffixAndDimension(NexusProperties);
        if (StringUtils.isNotBlank(pair.getLeft())) {
            tableName = tableName + "_" + pair.getLeft();
        }
        return createEmbeddingStore(tableName, pair.getRight());
    }

    private EmbeddingStore<TextSegment> createEmbeddingStore(String tableName, int dimension) {
        // 正则表达式匹�?
        String regex = "jdbc:postgresql://([^:/]+):(\\d+)/(\\w+).+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dataBaseUrl);

        String host = "";
        String port = "";
        String databaseName = "";
        if (matcher.matches()) {
            host = matcher.group(1);
            port = matcher.group(2);
            databaseName = matcher.group(3);

            log.info("Host: " + host);
            log.info("Port: " + port);
            log.info("Database: " + databaseName);
        } else {
            throw new RuntimeException("parse url error");
        }
        log.info("Creating PgVectorEmbeddingStore with table name:{},dimension:{}", tableName, dimension);
        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(Integer.parseInt(port))
                .database(databaseName)
                .user(dataBaseUserName)
                .password(dataBasePassword)
                .dimension(dimension)
                .createTable(true)
                .dropTableFirst(false)
                .table(tableName)
                .build();
    }

}
