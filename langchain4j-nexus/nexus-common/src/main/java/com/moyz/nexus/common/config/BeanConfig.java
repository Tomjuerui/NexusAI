package com.moyz.nexus.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moyz.nexus.common.base.SearchEngineRespTypeHandler;
import com.moyz.nexus.common.base.UUIDTypeHandler;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.dto.SearchEngineResp;
import com.moyz.nexus.common.entity.AiModel;
import com.moyz.nexus.common.service.ModelPlatformService;
import com.moyz.nexus.common.languagemodel.DashScopeEmbeddingModelService;
import com.moyz.nexus.common.languagemodel.OpenAiEmbeddingModelService;
import com.moyz.nexus.common.util.NexusPropertiesUtil;
import com.moyz.nexus.common.util.LocalDateTimeUtil;
import com.moyz.nexus.common.util.SpringUtil;
import com.pgvector.PGvector;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15.BgeSmallZhV15EmbeddingModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.UUID;

@Slf4j
@Configuration
public class BeanConfig {

    @Resource
    private NexusProperties NexusProperties;

    @Bean
    public RestTemplate restTemplate() {
        log.info("Configuration:create restTemplate");
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 设置建立连接超时时间 毫秒
        requestFactory.setConnectTimeout(60000);
        // 设置读取数据超时时间 毫秒
        requestFactory.setReadTimeout(60000);
        RestTemplate restTemplate = new RestTemplate();
        // 注册LOG拦截�?
        // restTemplate.setInterceptors(Lists.newArrayList(new
        // LogClientHttpRequestInterceptor()));
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));

        return restTemplate;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        log.info("Configuration:create objectMapper");
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().createXmlMapper(false).build();
        objectMapper.registerModules(LocalDateTimeUtil.getSimpleModule(), new JavaTimeModule(), new Jdk8Module());
        // 设置null值不参与序列�?字段不被显示)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean(name = "mainExecutor")
    @Primary
    public AsyncTaskExecutor mainExecutor() {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        log.info("mainExecutor,processorsNum:{}", processorsNum);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(processorsNum * 2);
        executor.setMaxPoolSize(100);
        return executor;
    }

    @Bean(name = "imagesExecutor")
    public AsyncTaskExecutor imagesExecutor() {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        log.info("imagesExecutor corePoolSize:{},maxPoolSize:{}", processorsNum, processorsNum * 2);
        executor.setCorePoolSize(processorsNum);
        executor.setMaxPoolSize(processorsNum * 2);
        return executor;
    }

    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource)
            throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        // 防止全表更新
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 动态表�?
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler(
                new EmbeddingTableNameHandler("nexus_knowledge_base_embedding", "nexus_ai_search_embedding",
                        "nexus_conversation_memory_embedding"));
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);

        bean.setPlugins(interceptor);
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/*.xml"));
        MybatisConfiguration configuration = bean.getConfiguration();
        if (null == configuration) {
            configuration = new MybatisConfiguration();
            bean.setConfiguration(configuration);
        }
        bean.getConfiguration().getTypeHandlerRegistry().register(PGvector.class, PostgresVectorTypeHandler.class);
        bean.getConfiguration().getTypeHandlerRegistry().register(SearchEngineResp.class,
                SearchEngineRespTypeHandler.class);
        bean.getConfiguration().getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        return bean.getObject();
    }

    /**
     * 初始化EmbeddingModel(单例),根据配置的embeddingModel选择不同的实�?
     *
     * @return EmbeddingModel实例
     */
    @Bean
    @DependsOn("initializer")
    public EmbeddingModel initEmbeddingModel() {
        if (NexusProperties.getEmbeddingModel().equals(NexusConstant.EmbeddingModel.ALL_MINILM_L6)) {
            return new AllMiniLmL6V2EmbeddingModel();
        }
        if (NexusProperties.getEmbeddingModel().equalsIgnoreCase(NexusConstant.EmbeddingModel.BGE_SMALL_ZH_V15)) {
            return new BgeSmallZhV15EmbeddingModel();
        }
        ModelPlatformService modelPlatformService = SpringUtil.getBean(ModelPlatformService.class);
        AiModel aiModel = NexusPropertiesUtil.getEmbeddingModelByProperty(NexusProperties);
        if (aiModel.getPlatform().equals(NexusConstant.ModelPlatform.DASHSCOPE)) {
            return new DashScopeEmbeddingModelService(aiModel, modelPlatformService.getByName(aiModel.getPlatform()))
                    .buildModel();
        } else if (aiModel.getPlatform().equals(NexusConstant.ModelPlatform.OPENAI)) {
            return new OpenAiEmbeddingModelService(aiModel, modelPlatformService.getByName(aiModel.getPlatform()))
                    .buildModel();
        } else {
            throw new RuntimeException("Unsupported embedding model: " + NexusProperties.getEmbeddingModel());
        }
    }

    // @Bean(name = "queryRouterRagService")
    // public RAGService queryRouterRagService() {
    // RAGService ragService = new RAGService("nexus_advanced_rag_query_embedding",
    // dataBaseUrl, dataBaseUserName, dataBasePassword);
    // ragService.init();
    // return ragService;
    // }

    @Bean(name = "beanValidator")
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
