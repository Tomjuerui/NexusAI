package com.moyz.nexus.common.workflow.def;

import com.moyz.nexus.common.workflow.data.NodeIOData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 工作流节点输入输出参数定�?
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class WfNodeIO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected String uuid;
    protected Integer type;
    protected String name;
    protected String title;
    protected Boolean required;

    /**
     * 检查数据是否合�?
     *
     * @param data 节点输入输出数据
     * @return 是否正确
     */
    public abstract boolean checkValue(NodeIOData data);
}
