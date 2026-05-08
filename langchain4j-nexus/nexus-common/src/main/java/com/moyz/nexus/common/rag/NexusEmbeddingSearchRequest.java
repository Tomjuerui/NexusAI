package com.moyz.nexus.common.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.Filter;

import java.util.List;

public class NexusEmbeddingSearchRequest extends EmbeddingSearchRequest {

    private List<String> ids;

    public NexusEmbeddingSearchRequest(List<String> ids, Embedding queryEmbedding, Integer maxResults, Double minScore, Filter filter) {
        super(queryEmbedding, maxResults, minScore, filter);
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public static NexusEmbeddingSearchRequestBuilder NexusBuilder() {
        return new NexusEmbeddingSearchRequestBuilder();
    }

    public static class NexusEmbeddingSearchRequestBuilder {
        private List<String> ids;
        private Embedding queryEmbedding;
        private Integer maxResults;
        private Double minScore;
        private Filter filter;

        NexusEmbeddingSearchRequestBuilder() {
        }

        public NexusEmbeddingSearchRequestBuilder ids(List<String> ids) {
            this.ids = ids;
            return this;
        }

        public NexusEmbeddingSearchRequestBuilder queryEmbedding(Embedding queryEmbedding) {
            this.queryEmbedding = queryEmbedding;
            return this;
        }

        public NexusEmbeddingSearchRequestBuilder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public NexusEmbeddingSearchRequestBuilder minScore(Double minScore) {
            this.minScore = minScore;
            return this;
        }

        public NexusEmbeddingSearchRequestBuilder filter(Filter filter) {
            this.filter = filter;
            return this;
        }

        public NexusEmbeddingSearchRequest build() {
            return new NexusEmbeddingSearchRequest(this.ids, this.queryEmbedding, this.maxResults, this.minScore, this.filter);
        }

        public String toString() {
            return "NexusEmbeddingSearchRequest.NexusEmbeddingSearchRequestBuilder(queryEmbedding=" + this.queryEmbedding + ", maxResults=" + this.maxResults + ", minScore=" + this.minScore + ", filter=" + this.filter + ")";
        }
    }
}