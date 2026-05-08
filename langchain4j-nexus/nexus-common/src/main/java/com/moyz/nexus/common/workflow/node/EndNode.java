package com.moyz.nexus.common.workflow.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.moyz.nexus.common.entity.WorkflowComponent;
import com.moyz.nexus.common.entity.WorkflowNode;
import com.moyz.nexus.common.workflow.*;
import com.moyz.nexus.common.workflow.data.NodeIOData;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.moyz.nexus.common.cosntant.NexusConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;


@Slf4j
public class EndNode extends AbstractWfNode {

    public EndNode(WorkflowComponent wfComponent, WorkflowNode nodeDef, WfState wfState, WfNodeState nodeState) {
        super(wfComponent, nodeDef, wfState, nodeState);
    }

    @Override
    protected NodeProcessResult onProcess() {
        List<NodeIOData> result = new ArrayList<>();
        JsonNode resultNode = node.getNodeConfig().get("result");
        String output = "";
        if (null == resultNode) {
            log.warn("EndNode result config is empty, nodeUuid: {}, title: {}", node.getUuid(), node.getTitle());
        } else {
            String resultTemplate = resultNode.asText();
            WfNodeIODataUtil.changeFilesContentToMarkdown(state.getInputs());
            output = WorkflowUtil.renderTemplate(resultTemplate, state.getInputs());
        }
        result.add(NodeIOData.createByText(DEFAULT_OUTPUT_PARAM_NAME, "", output));
        return NodeProcessResult.builder().content(result).build();
    }
}
