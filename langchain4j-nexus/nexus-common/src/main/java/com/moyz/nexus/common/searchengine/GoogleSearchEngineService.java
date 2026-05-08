package com.moyz.nexus.common.searchengine;

import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.dto.SearchReturn;
import com.moyz.nexus.common.dto.SearchReturnWebPage;
import com.moyz.nexus.common.interfaces.AbstractSearchEngineService;
import com.moyz.nexus.common.util.SearchEngineUtil;
import com.moyz.nexus.common.vo.GoogleSetting;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import static com.moyz.nexus.common.cosntant.NexusConstant.SysConfigKey.GOOGLE_SETTING;

@Slf4j
public class GoogleSearchEngineService extends AbstractSearchEngineService<GoogleSetting> {

    public GoogleSearchEngineService(InetSocketAddress proxy) {
        super(NexusConstant.SearchEngineName.GOOGLE, GOOGLE_SETTING, GoogleSetting.class, proxy);
    }

    @Override
    protected void initSearchEngine() {
        NexusGoogleCustomWebSearchEngine.NexusGoogleCustomWebSearchEngineBuilder builder = NexusGoogleCustomWebSearchEngine.builder()
                .apiKey(setting.getKey())
                .csi(setting.getCx())
                .siteRestrict(false)
                .includeImages(false)
                .logRequests(true)
                .logResponses(true);
        if (null != proxyAddress) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, proxyAddress));
        }
        searchEngine = builder.build();
    }

    @Override
    public boolean isEnabled() {
        return StringUtils.isNoneBlank(setting.getKey(), setting.getCx());
    }

    @Override
    public SearchReturn search(String searchTxt, String country, String language, Integer topN) {
        SearchReturn result = new SearchReturn();
        List<SearchReturnWebPage> items = new ArrayList<>();
        result.setItems(items);
        if (StringUtils.isBlank(searchTxt)) {
            log.warn("google search terms is empty");
            return result;
        }
        String tmpCountry = SearchEngineUtil.checkGoogleCountry(country) ? country : "cn";
        String tmpLang = SearchEngineUtil.checkGoogleLanguage(country) ? country : "zh-cn";
        try {
            WebSearchRequest webSearchRequest = WebSearchRequest.builder()
                    .searchTerms(searchTxt)
                    .language(tmpLang)
                    .geoLocation(tmpCountry)
                    .maxResults(topN)
                    .build();
            WebSearchResults webSearchResults = searchEngine.search(webSearchRequest);
            if (null != webSearchResults && webSearchResults.searchInformation().totalResults() > 0) {
                List<SearchReturnWebPage> wrapItems = webSearchResults.results()
                        .stream()
                        .map(
                                item -> SearchReturnWebPage.builder()
                                        .title(item.title())
                                        .link(item.url().toString())
                                        .snippet(item.snippet())
                                        .content(item.content())
                                        .build()
                        )
                        .toList();
                items.addAll(wrapItems);
            } else {
                log.warn("google search result is empty");
            }
        } catch (Exception e) {
            log.error("google search error", e);
        }
        return result;
    }

}
