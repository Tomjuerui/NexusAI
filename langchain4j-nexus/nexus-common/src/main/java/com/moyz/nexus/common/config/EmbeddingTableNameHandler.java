package com.moyz.nexus.common.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.moyz.nexus.common.util.NexusPropertiesUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class EmbeddingTableNameHandler implements TableNameHandler {

    private List<String> tableNames;

    //构造函数，构造动态表名处理器的时候，传递tableNames参数
    public EmbeddingTableNameHandler(String... tableNames) {
        this.tableNames = Arrays.asList(tableNames);
    }

    //动态表名接口实现方�?
    @Override
    public String dynamicTableName(String sql, String tableName) {
        if (this.tableNames.contains(tableName) && StringUtils.isNotBlank(NexusPropertiesUtil.EMBEDDING_TABLE_SUFFIX)) {
            return tableName + "_" + NexusPropertiesUtil.EMBEDDING_TABLE_SUFFIX;
        } else {
            return tableName;   //表名原样返回
        }
    }
}
