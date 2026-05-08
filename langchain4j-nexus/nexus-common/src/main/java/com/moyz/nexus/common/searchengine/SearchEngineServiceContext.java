package com.moyz.nexus.common.searchengine;

import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.interfaces.AbstractSearchEngineService;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Search engine context. strategy design model
 */
@Slf4j
public class SearchEngineServiceContext {
    public static final Map<String, AbstractSearchEngineService<?>> NAME_TO_SEARCHER = new LinkedHashMap<>();

    private SearchEngineServiceContext() {
    }

    public static void addWebSearcher(String engineName, AbstractSearchEngineService<?> searcher) {
        NAME_TO_SEARCHER.put(engineName, searcher);
    }

    public static AbstractSearchEngineService<?> getService(String searcherName) {
        AbstractSearchEngineService<?> searcher = NAME_TO_SEARCHER.get(searcherName);
        if (null == searcher) {
            log.warn("︿︿�?Can not find {}, use the default engine GOOGLE ︿︿�?, searcherName);
            return NAME_TO_SEARCHER.get(NexusConstant.SearchEngineName.GOOGLE);
        } else {
            return searcher;
        }
    }

    public static Map<String, AbstractSearchEngineService<?>> getAllService() {
        return NAME_TO_SEARCHER;
    }
}
