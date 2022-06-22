/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.OperateEntity
 *  io.camunda.operate.util.ElasticsearchUtil
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.search.SearchHits
 *  org.elasticsearch.search.aggregations.Aggregations
 *  org.springframework.beans.factory.annotation.Autowired
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.OperateEntity;
import io.camunda.operate.util.ElasticsearchUtil;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractReader {
    @Autowired
    protected RestHighLevelClient esClient;
    @Autowired
    protected ObjectMapper objectMapper;

    protected <T extends OperateEntity> List<T> scroll(SearchRequest searchRequest, Class<T> clazz) throws IOException {
        return ElasticsearchUtil.scroll((SearchRequest)searchRequest, clazz, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient);
    }

    protected <T extends OperateEntity> List<T> scroll(SearchRequest searchRequest, Class<T> clazz, Consumer<Aggregations> aggsProcessor) throws IOException {
        return ElasticsearchUtil.scroll((SearchRequest)searchRequest, clazz, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient, null, aggsProcessor);
    }

    protected <T extends OperateEntity> List<T> scroll(SearchRequest searchRequest, Class<T> clazz, Consumer<SearchHits> searchHitsProcessor, Consumer<Aggregations> aggsProcessor) throws IOException {
        return ElasticsearchUtil.scroll((SearchRequest)searchRequest, clazz, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient, searchHitsProcessor, aggsProcessor);
    }
}
