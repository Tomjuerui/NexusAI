package com.moyz.nexus.common.workflow.edge;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConditionalEdge extends Edge {
    private String sourceHandle;
}
