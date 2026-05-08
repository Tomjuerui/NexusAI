package com.moyz.nexus.common.service;

import com.moyz.nexus.common.config.NexusProperties;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.file.AliyunOssFileHelper;
import com.moyz.nexus.common.file.AliyunOssFileOperator;
import com.moyz.nexus.common.file.LocalFileOperator;
import com.moyz.nexus.common.rag.*;
import com.moyz.nexus.common.util.AesUtil;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Initializer {

    @Value("${local.images}")
    private String imagePath;
    @Value("${local.tmp-images}")
    private String tmpImagePath;
    @Value("${local.thumbnails}")
    private String thumbnailsPath;
    @Value("${local.files}")
    private String filePath;
    @Value("${local.chat-memory}")
    private String chatMemoryPath;
    @Resource
    private NexusProperties NexusProperties;
    @Resource
    private AiModelService aiModelService;
    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private AliyunOssFileHelper aliyunOssFileHelper;

    @Lazy
    @Resource
    private GraphStore kbGraphStore;
    @Lazy
    @Resource
    private EmbeddingStore<TextSegment> kbEmbeddingStore;
    @Lazy
    @Resource
    private EmbeddingStore<TextSegment> convMemoryEmbeddingStore;
    @Lazy
    @Resource
    private EmbeddingStore<TextSegment> aiSearchEmbeddingStore;
    @Lazy
    @Resource
    private EmbeddingModel embeddingModel;

    /**
     * 应用初始�?
     */
    @PostConstruct
    public void init() {
        if (NexusProperties.getEncrypt().getAesKey().equals("Ap9da0CopbjiKGc1")) {
            throw new RuntimeException("不能使用默认的AES key，请设置属于你自己的Key，AES相关的加解密都会用到该key，设置路�? application.yml => nexus.encrypt.aes-key");
        }
        sysConfigService.loadAndCache();
        aiModelService.init();
        checkAndInitFileOperator();

        AesUtil.AES_KEY = NexusProperties.getEncrypt().getAesKey();

        // 使用召回内容来源(RetrieveContentFrom)做为RAG名称以区分不同RAG实例
        EmbeddingRagContext.add(new EmbeddingRag(NexusConstant.RetrieveContentFrom.KNOWLEDGE_BASE, embeddingModel, kbEmbeddingStore));
        EmbeddingRagContext.add(new EmbeddingRag(NexusConstant.RetrieveContentFrom.CONV_MEMORY, embeddingModel, convMemoryEmbeddingStore));
        EmbeddingRagContext.add(new EmbeddingRag(NexusConstant.RetrieveContentFrom.WEB, embeddingModel, aiSearchEmbeddingStore));

        GraphRagContext.add(new GraphRag(NexusConstant.RetrieveContentFrom.KNOWLEDGE_BASE, kbGraphStore));
    }

    /**
     * 10分钟重刷一次配置信�?
     */
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedDelay = 10 * 60 * 1000)
    public void reloadConfig() {
        sysConfigService.loadAndCache();
    }

    public void checkAndInitFileOperator() {
        log.info("Initializing file operator...");
        LocalFileOperator.checkAndCreateDir(imagePath);
        LocalFileOperator.checkAndCreateDir(tmpImagePath);
        LocalFileOperator.checkAndCreateDir(thumbnailsPath);
        LocalFileOperator.checkAndCreateDir(filePath);
        LocalFileOperator.checkAndCreateDir(chatMemoryPath);
        LocalFileOperator.init(imagePath, filePath);
        AliyunOssFileOperator.init(aliyunOssFileHelper);
    }
}
