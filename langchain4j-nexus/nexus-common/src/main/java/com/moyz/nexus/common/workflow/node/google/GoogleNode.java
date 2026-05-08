package com.moyz.nexus.common.workflow.node.google;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.dto.SearchReturn;
import com.moyz.nexus.common.dto.SearchReturnWebPage;
import com.moyz.nexus.common.entity.WorkflowComponent;
import com.moyz.nexus.common.entity.WorkflowNode;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.searchengine.SearchEngineServiceContext;
import com.moyz.nexus.common.util.JsonUtil;
import com.moyz.nexus.common.workflow.NodeProcessResult;
import com.moyz.nexus.common.workflow.WfNodeState;
import com.moyz.nexus.common.workflow.WfState;
import com.moyz.nexus.common.workflow.WorkflowUtil;
import com.moyz.nexus.common.workflow.data.NodeIOData;
import com.moyz.nexus.common.workflow.node.AbstractWfNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.moyz.nexus.common.cosntant.NexusConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;
import static com.moyz.nexus.common.enums.ErrorEnum.*;

/**
 * 以摘要模式进行搜�?
 */
@Slf4j
public class GoogleNode extends AbstractWfNode {

    public GoogleNode(WorkflowComponent wfComponent, WorkflowNode nodeDef, WfState wfState, WfNodeState nodeState) {
        super(wfComponent, nodeDef, wfState, nodeState);
    }

    @Override
    protected NodeProcessResult onProcess() {
        ObjectNode objectConfig = node.getNodeConfig();
        if (objectConfig.isEmpty()) {
            throw new BaseException(A_WF_NODE_CONFIG_NOT_FOUND);
        }
        GoogleNodeConfig nodeConfigObj = JsonUtil.fromJson(objectConfig, GoogleNodeConfig.class);
        if (null == nodeConfigObj) {
            log.warn("找不到Google搜索节点的配�?);
            throw new BaseException(A_WF_NODE_CONFIG_ERROR);
        }
        log.info("GoogleNode config:{}", nodeConfigObj);
        String query;
        if (StringUtils.isNotBlank(nodeConfigObj.getQuery())) {
            query = WorkflowUtil.renderTemplate(nodeConfigObj.getQuery(), state.getInputs());
        } else {
            query = getFirstInputText();
        }
        if (StringUtils.isBlank(query)) {
            log.error("搜索字不能为�?);
            throw new BaseException(A_SEARCH_QUERY_IS_EMPTY);
        }
        log.info("GoogleNode query:{}", query);
        SearchReturn searchResult = SearchEngineServiceContext.getService(NexusConstant.SearchEngineName.GOOGLE).search(query, nodeConfigObj.getCountry(), nodeConfigObj.getLanguage(), nodeConfigObj.getTopN());
        if (StringUtils.isNotBlank(searchResult.getErrorMessage())) {
            log.error("Google search error:{}", searchResult.getErrorMessage());
        }
        StringBuilder respText = new StringBuilder();
        for (SearchReturnWebPage searchReturn : searchResult.getItems()) {
            respText.append(searchReturn.getSnippet());
        }
        return NodeProcessResult
                .builder()
                .content(List.of(NodeIOData.createByText(DEFAULT_OUTPUT_PARAM_NAME, "", respText.toString())))
                .build();
    }
}
