package com.moyz.nexus.common.util;

import com.moyz.nexus.common.vo.GraphSearchCondition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class GraphStoreUtil {

    private GraphStoreUtil() {
    }

    public static String buildWhereClause(GraphSearchCondition search, String alias) {
        if (null == search) {
            return StringUtils.EMPTY;
        }
        StringBuilder whereClause = new StringBuilder();
        if (CollectionUtils.isNotEmpty(search.getNames())) {
            List<String> nameArgs = new ArrayList<>();
            for (int i = 0; i < search.getNames().size(); i++) {
                nameArgs.add("$" + alias + "_name_" + i);
            }
            whereClause.append(String.format("(%s.name in [%s])", alias, String.join(",", nameArgs)));
        }
        //Metadata直接拼接字符�?
        if (null != search.getMetadataFilter()) {
            if (!whereClause.isEmpty()) {
                whereClause.append(" and ");
            }
            NexusApacheAgeJSONFilterMapper NexusJSONFilterMapper = new NexusApacheAgeJSONFilterMapper("metadata");
            NexusJSONFilterMapper.setAlias(alias);
            String metadataWhereClause = NexusJSONFilterMapper.map(search.getMetadataFilter());
            whereClause.append(metadataWhereClause);
        }

        return whereClause.toString();
    }

    public static Map<String, Object> buildWhereArgs(GraphSearchCondition search, String alias) {
        if (null == search) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(search.getNames())) {
            for (int i = 0; i < search.getNames().size(); i++) {
                result.put(alias + "_name_" + i, search.getNames().get(i));
            }
        }
        return result;
    }

    public static String buildSetClause(Map<String, Object> metadata) {
        //Apache AGE不支持直接更新property中的Map或List，只能直接替换，否则会出现异常：ERROR:  SET clause doesn't not support updating maps or lists in a property
        StringBuilder setClause = new StringBuilder();
        if (MapUtils.isNotEmpty(metadata)) {
            setClause.append(",v.metadata=$new_metadata");
        }
        return setClause.toString();
    }

    public static Map<String, Object> buildSetArgs(Map<String, Object> metadata) {
        Map<String, Object> result = new HashMap<>();
        if (MapUtils.isNotEmpty(metadata)) {
            result.put("new_metadata", metadata);
        }

        return result;
    }
}
