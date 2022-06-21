package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.ProcessEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.indices.ProcessIndex;
import io.camunda.operate.util.ElasticsearchUtil;
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
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
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
public class ProcessReader extends AbstractReader {
   private static final Logger logger = LoggerFactory.getLogger(ProcessReader.class);
   @Autowired
   private ProcessIndex processType;

   public String getDiagram(Long processDefinitionKey) {
      IdsQueryBuilder q = QueryBuilders.idsQuery().addIds(new String[]{processDefinitionKey.toString()});
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.processType.getAlias()})).source((new SearchSourceBuilder()).query(q).fetchSource("bpmnXml", (String)null));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value == 1L) {
            Map result = response.getHits().getHits()[0].getSourceAsMap();
            return (String)result.get("bpmnXml");
         } else if (response.getHits().getTotalHits().value > 1L) {
            throw new NotFoundException(String.format("Could not find unique process with id '%s'.", processDefinitionKey));
         } else {
            throw new NotFoundException(String.format("Could not find process with id '%s'.", processDefinitionKey));
         }
      } catch (IOException var6) {
         String message = String.format("Exception occurred, while obtaining the process diagram: %s", var6.getMessage());
         logger.error(message, var6);
         throw new OperateRuntimeException(message, var6);
      }
   }

   public ProcessEntity getProcess(Long processDefinitionKey) {
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.processType.getAlias()})).source((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", processDefinitionKey)));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value == 1L) {
            return this.fromSearchHit(response.getHits().getHits()[0].getSourceAsString());
         } else if (response.getHits().getTotalHits().value > 1L) {
            throw new NotFoundException(String.format("Could not find unique process with key '%s'.", processDefinitionKey));
         } else {
            throw new NotFoundException(String.format("Could not find process with key '%s'.", processDefinitionKey));
         }
      } catch (IOException var5) {
         String message = String.format("Exception occurred, while obtaining the process: %s", var5.getMessage());
         logger.error(message, var5);
         throw new OperateRuntimeException(message, var5);
      }
   }

   private ProcessEntity fromSearchHit(String processString) {
      return (ProcessEntity)ElasticsearchUtil.fromSearchHit(processString, this.objectMapper, ProcessEntity.class);
   }

   public Map getProcessesGrouped() {
      String groupsAggName = "group_by_bpmnProcessId";
      String processesAggName = "processes";
      AggregationBuilder agg = ((TermsAggregationBuilder)AggregationBuilders.terms("group_by_bpmnProcessId").field("bpmnProcessId")).size(10000).subAggregation(AggregationBuilders.topHits("processes").fetchSource(new String[]{"id", "name", "version", "bpmnProcessId"}, (String[])null).size(100).sort("version", SortOrder.DESC));
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.processType.getAlias()})).source((new SearchSourceBuilder()).aggregation(agg).size(0));

      try {
         SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         Terms groups = (Terms)searchResponse.getAggregations().get("group_by_bpmnProcessId");
         Map result = new HashMap();
         groups.getBuckets().stream().forEach((b) -> {
            String bpmnProcessId = b.getKeyAsString();
            result.put(bpmnProcessId, new ArrayList());
            TopHits processes = (TopHits)b.getAggregations().get("processes");
            SearchHit[] hits = processes.getHits().getHits();
            SearchHit[] var6 = hits;
            int var7 = hits.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               SearchHit searchHit = var6[var8];
               ProcessEntity processEntity = this.fromSearchHit(searchHit.getSourceAsString());
               ((List)result.get(bpmnProcessId)).add(processEntity);
            }

         });
         return result;
      } catch (IOException var8) {
         String message = String.format("Exception occurred, while obtaining grouped processes: %s", var8.getMessage());
         logger.error(message, var8);
         throw new OperateRuntimeException(message, var8);
      }
   }

   public Map getProcesses() {
      Map map = new HashMap();
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.processType.getAlias()})).source(new SearchSourceBuilder());

      try {
         List processesList = this.scroll(searchRequest);
         Iterator var7 = processesList.iterator();

         while(var7.hasNext()) {
            ProcessEntity processEntity = (ProcessEntity)var7.next();
            map.put(processEntity.getKey(), processEntity);
         }

         return map;
      } catch (IOException var6) {
         String message = String.format("Exception occurred, while obtaining processes: %s", var6.getMessage());
         logger.error(message, var6);
         throw new OperateRuntimeException(message, var6);
      }
   }

   public Map getProcessesWithFields(int maxSize, String... fields) {
      Map map = new HashMap();
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.processType.getAlias()})).source((new SearchSourceBuilder()).size(maxSize).fetchSource(fields, (String[])null));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         response.getHits().forEach((hit) -> {
            ProcessEntity entity = this.fromSearchHit(hit.getSourceAsString());
            map.put(entity.getKey(), entity);
         });
         return map;
      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining processes: %s", var7.getMessage());
         logger.error(message, var7);
         throw new OperateRuntimeException(message, var7);
      }
   }

   public Map getProcessesWithFields(String... fields) {
      return this.getProcessesWithFields(1000, fields);
   }

   private List scroll(SearchRequest searchRequest) throws IOException {
      return ElasticsearchUtil.scroll(searchRequest, ProcessEntity.class, this.objectMapper, this.esClient);
   }
}
