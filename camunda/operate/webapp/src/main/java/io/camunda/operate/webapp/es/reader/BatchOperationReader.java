package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.BatchOperationEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.BatchOperationTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.rest.dto.operation.BatchOperationRequestDto;
import io.camunda.operate.webapp.security.UserService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BatchOperationReader {
   private static final Logger logger = LoggerFactory.getLogger(BatchOperationReader.class);
   @Autowired
   private BatchOperationTemplate batchOperationTemplate;
   @Autowired
   private UserService userService;
   @Autowired
   private RestHighLevelClient esClient;
   @Autowired
   private ObjectMapper objectMapper;

   public List getBatchOperations(BatchOperationRequestDto batchOperationRequestDto) {
      SearchRequest searchRequest = this.createSearchRequest(batchOperationRequestDto);

      try {
         SearchResponse searchResponse = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         List batchOperationEntities = ElasticsearchUtil.mapSearchHits(searchResponse.getHits().getHits(), (sh) -> {
            BatchOperationEntity entity = (BatchOperationEntity)ElasticsearchUtil.fromSearchHit(sh.getSourceAsString(), this.objectMapper, BatchOperationEntity.class);
            entity.setSortValues(sh.getSortValues());
            return entity;
         });
         if (batchOperationRequestDto.getSearchBefore() != null) {
            Collections.reverse(batchOperationEntities);
         }

         return batchOperationEntities;
      } catch (IOException var6) {
         String message = String.format("Exception occurred, while getting page of batch operations list: %s", var6.getMessage());
         logger.error(message, var6);
         throw new OperateRuntimeException(message, var6);
      }
   }

   private SearchRequest createSearchRequest(BatchOperationRequestDto batchOperationRequestDto) {
      QueryBuilder queryBuilder = QueryBuilders.termQuery("username", this.userService.getCurrentUser().getUsername());
      Object[] searchAfter = batchOperationRequestDto.getSearchAfter();
      Object[] searchBefore = batchOperationRequestDto.getSearchBefore();
      FieldSortBuilder sort1;
      SortBuilder sort2;
      Object[] querySearchAfter;
      if (searchAfter == null && searchBefore != null) {
         sort1 = ((FieldSortBuilder)(new FieldSortBuilder("endDate")).order(SortOrder.ASC)).missing("_last");
         sort2 = (new FieldSortBuilder("startDate")).order(SortOrder.ASC);
         querySearchAfter = searchBefore;
      } else {
         sort1 = ((FieldSortBuilder)(new FieldSortBuilder("endDate")).order(SortOrder.DESC)).missing("_first");
         sort2 = (new FieldSortBuilder("startDate")).order(SortOrder.DESC);
         querySearchAfter = searchAfter;
      }

      SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource().query(QueryBuilders.constantScoreQuery(queryBuilder)).sort(sort1).sort(sort2).size(batchOperationRequestDto.getPageSize());
      if (querySearchAfter != null) {
         sourceBuilder.searchAfter(querySearchAfter);
      }

      return Requests.searchRequest(new String[]{this.batchOperationTemplate.getAlias()}).source(sourceBuilder);
   }
}
