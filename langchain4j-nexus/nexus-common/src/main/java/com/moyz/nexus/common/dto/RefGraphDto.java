package com.moyz.nexus.common.dto;

import com.moyz.nexus.common.vo.GraphEdge;
import com.moyz.nexus.common.vo.GraphVertex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefGraphDto {
    private List<String> entitiesFromQuestion;
    private List<GraphVertex> vertices;
    private List<GraphEdge> edges;
}
