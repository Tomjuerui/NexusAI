package com.moyz.nexus.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraphVertex {

    private String id;
    //Apache AGE暂时不支持多标签
    private String label;
    private String name;

    /**
     * 如对应的文本段id
     */
    @JsonProperty("text_segment_id")
    private String textSegmentId;
    private String description;

    /**
     * �?kb_uuid=>123,kb_item_uuid=>'22222,3333'
     */
    private Map<String, Object> metadata;
}
