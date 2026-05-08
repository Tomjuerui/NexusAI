package com.moyz.nexus.common.workflow.node;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moyz.nexus.common.entity.WorkflowComponent;
import com.moyz.nexus.common.entity.WorkflowNode;
import com.moyz.nexus.common.enums.WfIODataTypeEnum;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.util.CollectionUtil;
import com.moyz.nexus.common.util.JsonUtil;
import com.moyz.nexus.common.util.SpringUtil;
import com.moyz.nexus.common.workflow.NodeProcessResult;
import com.moyz.nexus.common.workflow.WfNodeInputConfig;
import com.moyz.nexus.common.workflow.WfNodeState;
import com.moyz.nexus.common.workflow.WfState;
import com.moyz.nexus.common.workflow.data.NodeIOData;
import com.moyz.nexus.common.workflow.def.WfNodeIO;
import com.moyz.nexus.common.workflow.def.WfNodeParamRef;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.moyz.nexus.common.cosntant.NexusConstant.WorkflowConstant.*;
import static com.moyz.nexus.common.enums.ErrorEnum.A_WF_NODE_CONFIG_ERROR;
import static com.moyz.nexus.common.enums.ErrorEnum.A_WF_NODE_CONFIG_NOT_FOUND;

/**
 * 节点实例-运行�?
 */
@Data
@Slf4j
public abstract class AbstractWfNode {

    protected WorkflowComponent wfComponent;
    protected WfState wfState;
    @Getter
    protected WfNodeState state;
    protected WorkflowNode node;

    public AbstractWfNode(WorkflowComponent wfComponent, WorkflowNode node, WfState wfState, WfNodeState nodeState) {
        this.wfState = wfState;
        this.wfComponent = wfComponent;
        this.state = nodeState;
        this.node = node;
    }

    public void initInput() {
        WfNodeInputConfig nodeInputConfig = node.getInputConfig();
        if (null == nodeInputConfig) {
            log.info("节点输入参数没有配置");
            return;
        }
        if (wfState.getCompletedNodes().isEmpty()) {
            log.info("没有上游节点，当前节点为开始节�?);
            state.getInputs().addAll(wfState.getInput());
            return;
        }

        List<NodeIOData> inputs = new ArrayList<>();

        //将上游节点的输出转成当前节点的输�?
        List<NodeIOData> upstreamOutputs = wfState.getLatestOutputs();
        if (!upstreamOutputs.isEmpty()) {
            inputs.addAll(new ArrayList<>(CollectionUtil.deepCopy(upstreamOutputs)));
        } else {
            log.warn("upstream output params is empty");
        }
        //处理引用类型的输入参数，非开始节点只有引用类型输入参�?
        List<WfNodeParamRef> refInputDefs = nodeInputConfig.getRefInputs();
        inputs.addAll(changeRefersToNodeIODatas(refInputDefs));

        //根据节点的输入参数定义，刷选出符合要求的输入参�?
        WfNodeInputConfig inputConfig = node.getInputConfig();
        List<String> defInputNames = inputConfig.getRefInputs().stream().map(WfNodeParamRef::getName).collect(Collectors.toList());
        defInputNames.addAll(inputConfig.getUserInputs().stream().map(WfNodeIO::getName).toList());
        List<NodeIOData> needInputs = inputs.stream().filter(item -> {
            String needInputName = item.getName();
            //上流节点的默认输出参�?output)，改成input即可
            if (DEFAULT_OUTPUT_PARAM_NAME.equals(needInputName)) {
                item.setName(DEFAULT_INPUT_PARAM_NAME);
                return true;
            }
            return defInputNames.contains(needInputName);
        }).toList();
        state.getInputs().addAll(needInputs);
    }

    /**
     * 查找引用节点的参数并转成输入输出参数
     *
     * @param referParams 引用类型的定义列�?
     */
    private List<NodeIOData> changeRefersToNodeIODatas(List<WfNodeParamRef> referParams) {
        List<NodeIOData> result = new ArrayList<>();
        for (WfNodeParamRef referParam : referParams) {
            String nodeUuid = referParam.getNodeUuid();
            String nodeParamName = referParam.getNodeParamName();
            NodeIOData newInput = createByReferParam(nodeUuid, nodeParamName);
            if (null != newInput) {
                newInput.setName(referParam.getName());
                result.add(newInput);
            } else {
                log.warn("Can not find reference node output param,refNodeId:{},refNodeOutputName:{}", nodeUuid, nodeParamName);
            }
        }
        return result;
    }

    public NodeIOData createByReferParam(String refNodeUuid, String refNodeParamName) {
        Optional<NodeIOData> hitDataOpt = wfState.getIOByNodeUuid(refNodeUuid)
                .stream()
                .filter(wfNodeIOData -> wfNodeIOData.getName().equalsIgnoreCase(refNodeParamName))
                .findFirst();
        return hitDataOpt.<NodeIOData>map(SerializationUtils::clone).orElse(null);
    }

    public NodeProcessResult process(Consumer<WfNodeState> inputConsumer, Consumer<WfNodeState> outputConsumer) {
        log.info("↓↓↓↓�?node process start,name:{},uuid:{}", node.getTitle(), node.getUuid());
        state.setProcessStatus(NODE_PROCESS_STATUS_DOING);
        initInput();
        //HumanFeedback的情�?
        Object humanFeedbackState = state.data().get(HUMAN_FEEDBACK_KEY);
        if (null != humanFeedbackState) {
            String userInput = humanFeedbackState.toString();
            if (StringUtils.isNotBlank(userInput)) {
                state.getInputs().add(NodeIOData.createByText(HUMAN_FEEDBACK_KEY, "default", userInput));
            }
        }
        if (null != inputConsumer) {
            inputConsumer.accept(state);
        }
        log.info("--node input:{}", JsonUtil.toJson(state.getInputs()));
        NodeProcessResult processResult;
        try {
            processResult = onProcess();
        } catch (Exception e) {
            state.setProcessStatus(NODE_PROCESS_STATUS_FAIL);
            state.setProcessStatusRemark("process error:" + e.getMessage());
            wfState.setProcessStatus(WORKFLOW_PROCESS_STATUS_FAIL);
            log.info("↑↑↑↑�?node process error,name:{},uuid:{},error", node.getTitle(), node.getUuid(), e);
            if (null != outputConsumer) {
                outputConsumer.accept(state);
            }
            throw new RuntimeException(e);
        }

        if (!processResult.getContent().isEmpty()) {
            state.setOutputs(processResult.getContent());
        }
        state.setProcessStatus(NODE_PROCESS_STATUS_SUCCESS);
        //交由langgraph4j执行下一个节�?
//        if (nextNode != null) {
//            nextNode.getWfNodeState().setInput(output);
//            nextNode.process();
//        } else {
//            wfNodeState.setOutput(output);
//            wfNodeState.setProcessState(NODE_STATE_SUCCESS);
//            wfNodeState.setProcessStateDesc("workflow complete");
//        }
        wfState.getCompletedNodes().add(this);
        log.info("↑↑↑↑�?node process end,name:{},uuid:{},output:{}", node.getTitle(), node.getUuid(), JsonUtil.toJson(state.getOutputs()));
        if (null != outputConsumer) {
            outputConsumer.accept(state);
        }
        return processResult;
    }

    protected abstract NodeProcessResult onProcess();

    protected String getFirstInputText() {
        String firstInputText;
        if (state.getInputs().size() > 1) {
            firstInputText = state.getInputs()
                    .stream()
                    .filter(item -> WfIODataTypeEnum.TEXT.getValue().equals(item.getContent().getType()) && !DEFAULT_INPUT_PARAM_NAME.equals(item.getName()))
                    .map(NodeIOData::valueToString)
                    .findFirst()
                    .orElse("");
        } else {
            firstInputText = state.getInputs().get(0).valueToString();
        }
        return firstInputText;
    }

    protected <T> T checkAndGetConfig(Class<T> clazz) {
        ObjectNode configObj = node.getNodeConfig();
        if (configObj.isEmpty()) {
            log.error("node config is empty,node uuid:{}", state.getUuid());
            throw new BaseException(A_WF_NODE_CONFIG_NOT_FOUND);
        }
        log.info("node config:{}", configObj);
        T nodeConfig = JsonUtil.fromJson(configObj, clazz);
        if (null == nodeConfig) {
            log.warn("找不到节点的配置,node uuid:{}", state.getUuid());
            throw new BaseException(A_WF_NODE_CONFIG_ERROR);
        }
        boolean configValid = true;
        try {
            Set<ConstraintViolation<T>> violations = SpringUtil.getBean("beanValidator", LocalValidatorFactoryBean.class).validate(nodeConfig);
            for (ConstraintViolation<T> violation : violations) {
                log.error(violation.getMessage());
                configValid = false;
            }
        } catch (Exception e) {
            log.error("节点配置校验失败,node uuid:{},error:{}", state.getUuid(), e.getMessage());
            configValid = false;
        }
        if (!configValid) {
            log.warn("节点配置错误,node uuid:{}", state.getUuid());
            throw new BaseException(A_WF_NODE_CONFIG_ERROR);
        }
        return nodeConfig;
    }

}
