/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.ProcessEntity
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.indices.ProcessIndex
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.IdsQueryBuilder
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.search.SearchHit
 *  org.elasticsearch.search.aggregations.AbstractAggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilder
 *  org.elasticsearch.search.aggregations.AggregationBuilders
 *  org.elasticsearch.search.aggregations.bucket.terms.Terms
 *  org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
 *  org.elasticsearch.search.aggregations.metrics.TopHits
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.ProcessEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.indices.ProcessIndex;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(ProcessReader.class);
    @Autowired
    private ProcessIndex processType;

    public String getDiagram(Long processDefinitionKey) {
        IdsQueryBuilder q = QueryBuilders.idsQuery().addIds(new String[]{processDefinitionKey.toString()});
        SearchRequest searchRequest = new SearchRequest(new String[]{this.processType.getAlias()}).source(new SearchSourceBuilder().query((QueryBuilder)q).fetchSource("bpmnXml", null));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value == 1L) {
                Map result = response.getHits().getHits()[0].getSourceAsMap();
                return (String)result.get("bpmnXml");
            }
            if (response.getHits().getTotalHits().value <= 1L) throw new NotFoundException(String.format("Could not find process with id '%s'.", processDefinitionKey));
            throw new NotFoundException(String.format("Could not find unique process with id '%s'.", processDefinitionKey));
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining the process diagram: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public ProcessEntity getProcess(Long processDefinitionKey) {
        SearchRequest searchRequest = new SearchRequest(new String[]{this.processType.getAlias()}).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"key", (Object)processDefinitionKey)));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value == 1L) {
                return this.fromSearchHit(response.getHits().getHits()[0].getSourceAsString());
            }
            if (response.getHits().getTotalHits().value <= 1L) throw new NotFoundException(String.format("Could not find process with key '%s'.", processDefinitionKey));
            throw new NotFoundException(String.format("Could not find unique process with key '%s'.", processDefinitionKey));
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining the process: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    private ProcessEntity fromSearchHit(String processString) {
        return (ProcessEntity)ElasticsearchUtil.fromSearchHit((String)processString, (ObjectMapper)this.objectMapper, ProcessEntity.class);
    }

    public Map<String, List<ProcessEntity>> getProcessesGrouped() {
        String groupsAggName = "group_by_bpmnProcessId";
        String processesAggName = "processes";
        AbstractAggregationBuilder agg = ((TermsAggregationBuilder)AggregationBuilders.terms((String)"group_by_bpmnProcessId").field("bpmnProcessId")).size(10000).subAggregation((AggregationBuilder)AggregationBuilders.topHits((String)"processes").fetchSource(new String[]{"id", "name", "version", "bpmnProcessId"}, null).size(100).sort("version", SortOrder.DESC));
        SearchRequest searchRequest = new SearchRequest(new String[]{this.processType.getAlias()}).source(new SearchSourceBuilder().aggregation((AggregationBuilder)agg).size(0));
        try {
            SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            Terms groups = (Terms)searchResponse.getAggregations().get("group_by_bpmnProcessId");
            HashMap<String, List<ProcessEntity>> result = new HashMap<String, List<ProcessEntity>>();
            groups.getBuckets().stream().forEach(b -> {
                SearchHit[] hits;
                String bpmnProcessId = b.getKeyAsString();
                result.put(bpmnProcessId, new ArrayList());
                TopHits processes = (TopHits)b.getAggregations().get("processes");
                SearchHit[] searchHitArray = hits = processes.getHits().getHits();
                int n = searchHitArray.length;
                int n2 = 0;
                while (n2 < n) {
                    SearchHit searchHit = searchHitArray[n2];
                    ProcessEntity processEntity = this.fromSearchHit(searchHit.getSourceAsString());
                    ((List)result.get(bpmnProcessId)).add(processEntity);
                    ++n2;
                }
            });
            return result;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining grouped processes: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public Map<Long, ProcessEntity> getProcesses() {
        HashMap<Long, ProcessEntity> map = new HashMap<Long, ProcessEntity>();
        SearchRequest searchRequest = new SearchRequest(new String[]{this.processType.getAlias()}).source(new SearchSourceBuilder());
        try {
            List<ProcessEntity> processesList = this.scroll(searchRequest);
            Iterator<ProcessEntity> iterator = processesList.iterator();
            while (iterator.hasNext()) {
                ProcessEntity processEntity = iterator.next();
                map.put(processEntity.getKey(), processEntity);
            }
            return map;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining processes: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public Map<Long, ProcessEntity> getProcessesWithFields(int maxSize, String ... fields) {
        HashMap<Long, ProcessEntity> map = new HashMap<Long, ProcessEntity>();
        SearchRequest searchRequest = new SearchRequest(new String[]{this.processType.getAlias()}).source(new SearchSourceBuilder().size(maxSize).fetchSource(fields, null));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            response.getHits().forEach(hit -> {
                ProcessEntity entity = this.fromSearchHit(hit.getSourceAsString());
                map.put(entity.getKey(), entity);
            });
            return map;
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining processes: %s", e.getMessage());
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public Map<Long, ProcessEntity> getProcessesWithFields(String ... fields) {
        return this.getProcessesWithFields(1000, fields);
    }

    private List<ProcessEntity> scroll(SearchRequest searchRequest) throws IOException {
        return ElasticsearchUtil.scroll((SearchRequest)searchRequest, ProcessEntity.class, (ObjectMapper)this.objectMapper, (RestHighLevelClient)this.esClient);
    }
}
