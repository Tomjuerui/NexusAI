package com.moyz.nexus.common.vo;

import com.moyz.nexus.common.entity.User;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.chat.ChatModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GraphIngestParams {
    private User user;
    private Document document;
    private int overlap;
    private String tokenEstimator;
    private ChatModel ChatModel;
    private List<String> identifyColumns;
    private List<String> appendColumns;
    private boolean isFreeToken;
}
