package com.moyz.nexus.common.workflow.node.documentextractor;

import com.moyz.nexus.common.entity.NexusFile;
import com.moyz.nexus.common.entity.WorkflowComponent;
import com.moyz.nexus.common.entity.WorkflowNode;
import com.moyz.nexus.common.enums.WfIODataTypeEnum;
import com.moyz.nexus.common.file.FileOperatorContext;
import com.moyz.nexus.common.service.FileService;
import com.moyz.nexus.common.util.SpringUtil;
import com.moyz.nexus.common.workflow.NodeProcessResult;
import com.moyz.nexus.common.workflow.WfNodeState;
import com.moyz.nexus.common.workflow.WfState;
import com.moyz.nexus.common.workflow.data.NodeIOData;
import com.moyz.nexus.common.workflow.data.NodeIODataFilesContent;
import com.moyz.nexus.common.workflow.data.NodeIODataTextContent;
import com.moyz.nexus.common.workflow.node.AbstractWfNode;
import dev.langchain4j.data.document.Document;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.moyz.nexus.common.cosntant.NexusConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;

/**
 * 【节点】文档解�?<br/>
 */
@Slf4j
public class DocumentExtractorNode extends AbstractWfNode {

    public DocumentExtractorNode(WorkflowComponent wfComponent, WorkflowNode nodeDef, WfState wfState, WfNodeState nodeState) {
        super(wfComponent, nodeDef, wfState, nodeState);
    }

    @Override
    public NodeProcessResult onProcess() {
        StringBuilder documentText = new StringBuilder();
        List<NodeIOData> list = state.getInputs();
        List<String> fileUuids = new ArrayList<>();
        for (NodeIOData nodeIOData : list) {
            if (WfIODataTypeEnum.FILES.getValue().equals(nodeIOData.getContent().getType())) {
                NodeIODataFilesContent filesContent = (NodeIODataFilesContent) nodeIOData.getContent();
                fileUuids.addAll(filesContent.getValue());
            }
        }
        FileService fileService = SpringUtil.getBean(FileService.class);
        //解析文档
        try {
            for (String uuid : fileUuids) {
                NexusFile NexusFile = fileService.getFile(uuid);
                Document document = FileOperatorContext.loadDocument(NexusFile);
                if (null == document) {
                    log.warn("{}的文件类�?{}无法解析，忽�?, NexusFile.getUuid(), NexusFile.getExt());
                    continue;
                }
                documentText.append(document.text());
            }
        } catch (Exception e) {
            log.error("解析文档失败", e);
        }
        NodeIODataTextContent dataContent = new NodeIODataTextContent();
        dataContent.setValue(documentText.toString());
        dataContent.setTitle("");
        List<NodeIOData> result = List.of(NodeIOData.builder().name(DEFAULT_OUTPUT_PARAM_NAME).content(dataContent).build());
        return NodeProcessResult.builder().content(result).build();
    }
}
