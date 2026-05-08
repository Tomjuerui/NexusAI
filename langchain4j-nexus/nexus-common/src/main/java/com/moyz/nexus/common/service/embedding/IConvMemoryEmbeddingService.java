package com.moyz.nexus.common.service.embedding;

import com.moyz.nexus.common.dto.KbItemEmbeddingDto;

import java.util.List;

public interface IConvMemoryEmbeddingService {
    List<KbItemEmbeddingDto> listByEmbeddingIds(List<String> embeddingIds);
}
