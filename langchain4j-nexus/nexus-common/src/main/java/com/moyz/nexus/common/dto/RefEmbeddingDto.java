package com.moyz.nexus.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefEmbeddingDto {
    private String embeddingId;
    private String text;
}
