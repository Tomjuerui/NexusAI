package com.moyz.nexus.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.nexus.common.dto.KbItemEmbeddingDto;
import com.moyz.nexus.common.dto.RefEmbeddingDto;
import com.moyz.nexus.common.entity.KnowledgeBaseQaRefEmbedding;
import com.moyz.nexus.common.mapper.KnowledgeBaseQaRecordReferenceMapper;
import com.moyz.nexus.common.service.embedding.IKnowledgeEmbeddingService;
import com.moyz.nexus.common.util.EmbeddingUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class KnowledgeBaseQaRecordReferenceService extends ServiceImpl<KnowledgeBaseQaRecordReferenceMapper, KnowledgeBaseQaRefEmbedding> {

    @Resource
    private IKnowledgeEmbeddingService iKnowledgeEmbeddingService;

    public List<RefEmbeddingDto> listRefEmbeddings(String aqRecordUuid) {
        List<KnowledgeBaseQaRefEmbedding> recordReferences = this.getBaseMapper().listByQaUuid(aqRecordUuid);
        if (CollectionUtils.isEmpty(recordReferences)) {
            return Collections.emptyList();
        }
        List<String> embeddingIds = recordReferences.stream().map(KnowledgeBaseQaRefEmbedding::getEmbeddingId).toList();
        if (CollectionUtils.isEmpty(embeddingIds)) {
            return Collections.emptyList();
        }
        List<KbItemEmbeddingDto> embeddings = iKnowledgeEmbeddingService.listByEmbeddingIds(embeddingIds);
        return EmbeddingUtil.itemToRefEmbeddingDto(embeddings);
    }
}
