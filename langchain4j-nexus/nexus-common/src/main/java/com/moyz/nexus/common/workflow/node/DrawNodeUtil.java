package com.moyz.nexus.common.workflow.node;

import com.moyz.nexus.common.entity.NexusFile;
import com.moyz.nexus.common.entity.Draw;
import com.moyz.nexus.common.entity.User;
import com.moyz.nexus.common.file.FileOperatorContext;
import com.moyz.nexus.common.languagemodel.AbstractImageModelService;
import com.moyz.nexus.common.service.FileService;
import com.moyz.nexus.common.util.SpringUtil;
import com.moyz.nexus.common.workflow.NodeProcessResult;
import com.moyz.nexus.common.workflow.data.NodeIOData;
import com.moyz.nexus.common.workflow.data.NodeIODataFilesContent;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static com.moyz.nexus.common.cosntant.NexusConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;

public class DrawNodeUtil {

    /**
     * 绘图并将结果组装以输出到下一节点
     *
     * @param user              执行用户
     * @param draw              绘图信息
     * @param imageModelService 绘图service
     * @return 节点处理结果
     */
    public static NodeProcessResult createResultContent(User user, Draw draw, AbstractImageModelService imageModelService) {
        List<String> images = imageModelService.generateImage(user, draw);
        FileService fileService = SpringUtil.getBean(FileService.class);
        String imageUrl = "";
        if (CollectionUtils.isNotEmpty(images)) {
            NexusFile NexusFile = fileService.saveImageFromUrl(user, images.get(0));
            imageUrl = FileOperatorContext.getFileUrl(NexusFile);
        }
        NodeIODataFilesContent datContent = new NodeIODataFilesContent();
        datContent.setValue(List.of(imageUrl));
        datContent.setTitle("");
        List<NodeIOData> result = List.of(NodeIOData.builder().name(DEFAULT_OUTPUT_PARAM_NAME).content(datContent).build());
        return NodeProcessResult.builder().content(result).build();
    }
}
