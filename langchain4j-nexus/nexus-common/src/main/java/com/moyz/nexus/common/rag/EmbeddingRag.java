package com.moyz.nexus.common.rag;

import com.moyz.nexus.common.interfaces.IRAGService;
import com.moyz.nexus.common.util.InputAdaptor;
import com.moyz.nexus.common.vo.InputAdaptorMsg;
import com.moyz.nexus.common.vo.RetrieverCreateParam;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.moyz.nexus.common.cosntant.NexusConstant.*;
import static com.moyz.nexus.common.vo.InputAdaptorMsg.TOKEN_TOO_MUCH_QUESTION;

@Slf4j
public class EmbeddingRag implements IRAGService {

    /**
     * RAG名称，用于区分不同的实例
     */
    @Getter
    private final String name;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    public EmbeddingRag(String name, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.name = name;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * 对文档切块、向量化并存储到数据�?
     *
     * @param document 知识库文�?
     * @param overlap  重叠token�?
     */
    @Override
    public void ingest(Document document, int overlap, String tokenEstimator, ChatModel ChatModel) {
        log.info("EmbeddingRag ingest,TokenCountEstimator:{}", tokenEstimator);
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(RAG_MAX_SEGMENT_SIZE_IN_TOKENS, overlap, TokenEstimatorFactory.create(tokenEstimator));
        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        embeddingStoreIngestor.ingest(document);
    }

    /**
     * 创建召回�?
     *
     * @param param 条件
     * @return ContentRetriever
     */
    @Override
    public NexusEmbeddingStoreContentRetriever createRetriever(RetrieverCreateParam param) {
        return NexusEmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(param.getMaxResults() <= 0 ? 3 : param.getMaxResults())
                .minScore(param.getMinScore() <= 0 ? RAG_MIN_SCORE : param.getMinScore())
                .filter(param.getFilter())
                .breakIfSearchMissed(param.isBreakIfSearchMissed())
                .build();
    }

    /**
     * 根据模型的contentWindow计算使用该模型最多召回的文档数量
     * <br/>以分块时的最大文本段对应的token数量{maxSegmentSizeInTokens}为计算因�?
     *
     * @param userQuestion   用户的问�?
     * @param maxInputTokens AI模型所能容纳的窗口大小
     * @return
     */
    public static int getRetrieveMaxResults(String userQuestion, int maxInputTokens) {
        if (maxInputTokens == 0) {
            return RAG_RETRIEVE_NUMBER_MAX;
        }
        InputAdaptorMsg inputAdaptorMsg = InputAdaptor.isQuestionValid(userQuestion, maxInputTokens);
        if (inputAdaptorMsg.getTokenTooMuch() == TOKEN_TOO_MUCH_QUESTION) {
            log.warn("用户问题太长了，没有足够的token数量留给召回的内�?);
            return 0;
        } else {
            int maxRetrieveDocLength = maxInputTokens - inputAdaptorMsg.getUserQuestionTokenCount();
            if (maxRetrieveDocLength > RAG_RETRIEVE_NUMBER_MAX * RAG_MAX_SEGMENT_SIZE_IN_TOKENS) {
                return RAG_RETRIEVE_NUMBER_MAX;
            } else {
                return maxRetrieveDocLength / RAG_MAX_SEGMENT_SIZE_IN_TOKENS;
            }
        }

    }
}
