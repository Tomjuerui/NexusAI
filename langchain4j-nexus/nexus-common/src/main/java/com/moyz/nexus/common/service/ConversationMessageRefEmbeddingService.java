package com.moyz.nexus.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.nexus.common.dto.KbItemEmbeddingDto;
import com.moyz.nexus.common.dto.RefEmbeddingDto;
import com.moyz.nexus.common.entity.ConversationMessageRefEmbedding;
import com.moyz.nexus.common.mapper.ConversationMessageRefEmbeddingMapper;
import com.moyz.nexus.common.service.embedding.pgvector.ConvMemoryEmbeddingService;
import com.moyz.nexus.common.util.EmbeddingUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ConversationMessageRefEmbeddingService extends ServiceImpl<ConversationMessageRefEmbeddingMapper, ConversationMessageRefEmbedding> {

    @Resource
    private ConvMemoryEmbeddingService convMemoryEmbeddingService;

    public List<RefEmbeddingDto> listRefEmbeddings(String msgUuid) {
        List<ConversationMessageRefEmbedding> recordReferences = this.getBaseMapper().listByMsgUuid(msgUuid);
        if (CollectionUtils.isEmpty(recordReferences)) {
            return Collections.emptyList();
        }
        List<String> embeddingIds = recordReferences.stream().map(ConversationMessageRefEmbedding::getEmbeddingId).toList();
        if (CollectionUtils.isEmpty(embeddingIds)) {
            return Collections.emptyList();
        }
        List<KbItemEmbeddingDto> embeddings = convMemoryEmbeddingService.listByEmbeddingIds(embeddingIds);
        return EmbeddingUtil.itemToRefEmbeddingDto(embeddings);
    }

}
