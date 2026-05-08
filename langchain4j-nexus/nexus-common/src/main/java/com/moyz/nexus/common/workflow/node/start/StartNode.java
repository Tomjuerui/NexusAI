package com.moyz.nexus.common.workflow.node.start;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.nexus.common.entity.WorkflowComponent;
import com.moyz.nexus.common.entity.WorkflowNode;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.util.JsonUtil;
import com.moyz.nexus.common.workflow.NodeProcessResult;
import com.moyz.nexus.common.workflow.WfNodeIODataUtil;
import com.moyz.nexus.common.workflow.WfNodeState;
import com.moyz.nexus.common.workflow.WfState;
import com.moyz.nexus.common.workflow.data.NodeIOData;
import com.moyz.nexus.common.workflow.node.AbstractWfNode;
import com.moyz.nexus.common.workflow.node.keywordextractor.KeywordExtractorNodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.moyz.nexus.common.cosntant.NexusConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;
import static com.moyz.nexus.common.enums.ErrorEnum.A_WF_NODE_CONFIG_ERROR;
import static com.moyz.nexus.common.enums.ErrorEnum.A_WF_NODE_CONFIG_NOT_FOUND;

@Slf4j
public class StartNode extends AbstractWfNode {

    public StartNode(WorkflowComponent wfComponent, WorkflowNode nodeDef, WfState wfState, WfNodeState nodeState) {
        super(wfComponent, nodeDef, wfState, nodeState);
    }

    @Override
    public NodeProcessResult onProcess() {
        ObjectNode objectConfig = node.getNodeConfig();
        if (null == objectConfig) {
            throw new BaseException(A_WF_NODE_CONFIG_NOT_FOUND);
        }
        List<NodeIOData> result;
        StartNodeConfig nodeConfigObj = JsonUtil.fromJson(objectConfig, StartNodeConfig.class);
        if (null == nodeConfigObj) {
            log.warn("找不到开始节点的配置");
            throw new BaseException(A_WF_NODE_CONFIG_ERROR);
        }
        if (StringUtils.isNotBlank(nodeConfigObj.getPrologue())) {
            result = List.of(NodeIOData.createByText(DEFAULT_OUTPUT_PARAM_NAME, "default", nodeConfigObj.getPrologue()));
        } else {
            result = WfNodeIODataUtil.changeInputsToOutputs(state.getInputs());
        }
        return NodeProcessResult.builder().content(result).build();
    }

}
