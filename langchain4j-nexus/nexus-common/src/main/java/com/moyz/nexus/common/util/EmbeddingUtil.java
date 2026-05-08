package com.moyz.nexus.common.util;

import com.moyz.nexus.common.dto.KbItemEmbeddingDto;
import com.moyz.nexus.common.dto.RefEmbeddingDto;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingUtil {

    public static List<RefEmbeddingDto> itemToRefEmbeddingDto(List<KbItemEmbeddingDto> embeddings) {
        List<RefEmbeddingDto> result = new ArrayList<>();
        for (KbItemEmbeddingDto embedding : embeddings) {
            RefEmbeddingDto newOne = RefEmbeddingDto.builder()
                    .embeddingId(embedding.getEmbeddingId())
                    .text(embedding.getText())
                    .build();
            result.add(newOne);
        }
        return result;
    }

}
