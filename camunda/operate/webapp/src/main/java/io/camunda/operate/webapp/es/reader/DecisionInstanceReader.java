package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.dmn.DecisionInstanceEntity;
import io.camunda.operate.entities.dmn.DecisionInstanceState;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.indices.DecisionIndex;
import io.camunda.operate.schema.templates.DecisionInstanceTemplate;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.dmn.DRDDataEntryDto;
import io.camunda.operate.webapp.rest.dto.dmn.DecisionInstanceDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceForListDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListQueryDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListRequestDto;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListResponseDto;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DecisionInstanceReader extends AbstractReader {
   private static final Logger logger = LoggerFactory.getLogger(DecisionInstanceReader.class);
   @Autowired
   private DecisionInstanceTemplate decisionInstanceTemplate;
   @Autowired
   private DecisionIndex decisionIndex;
   @Autowired
   private DateTimeFormatter dateTimeFormatter;
   @Autowired
   private OperateProperties operateProperties;

   public DecisionInstanceDto getDecisionInstance(String decisionInstanceId) {
      QueryBuilder query = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{QueryBuilders.idsQuery().addIds(new String[]{String.valueOf(decisionInstanceId)}), QueryBuilders.termQuery("id", decisionInstanceId)});
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.decisionInstanceTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(QueryBuilders.constantScoreQuery(query)));

      try {
         SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value == 1L) {
            DecisionInstanceEntity decisionInstance = (DecisionInstanceEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getHits()[0].getSourceAsString(), this.objectMapper, DecisionInstanceEntity.class);
            return (DecisionInstanceDto)DtoCreator.create((Object)decisionInstance, DecisionInstanceDto.class);
         } else if (response.getHits().getTotalHits().value > 1L) {
            throw new NotFoundException(String.format("Could not find unique decision instance with id '%s'.", decisionInstanceId));
         } else {
            throw new NotFoundException(String.format("Could not find decision instance with id '%s'.", decisionInstanceId));
         }
      } catch (IOException var6) {
         throw new OperateRuntimeException(var6.getMessage(), var6);
      }
   }

   public DecisionInstanceListResponseDto queryDecisionInstances(DecisionInstanceListRequestDto request) {
      DecisionInstanceListResponseDto result = new DecisionInstanceListResponseDto();
      List entities = this.queryDecisionInstancesEntities(request, result);
      result.setDecisionInstances(DtoCreator.create(entities, DecisionInstanceForListDto.class));
      return result;
   }

   private List queryDecisionInstancesEntities(DecisionInstanceListRequestDto request, DecisionInstanceListResponseDto result) {
      QueryBuilder query = this.createRequestQuery(request.getQuery());
      logger.debug("Decision instance search request: \n{}", query.toString());
      SearchSourceBuilder searchSourceBuilder = (new SearchSourceBuilder()).query(query).fetchSource((String[])null, new String[]{"result", "evaluatedInputs", "evaluatedOutputs"});
      this.applySorting(searchSourceBuilder, request);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.decisionInstanceTemplate).source(searchSourceBuilder);
      logger.debug("Search request will search in: \n{}", searchRequest.indices());

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         result.setTotalCount(response.getHits().getTotalHits().value);
         List decisionInstanceEntities = ElasticsearchUtil.mapSearchHits(response.getHits().getHits(), (sh) -> {
            DecisionInstanceEntity entity = (DecisionInstanceEntity)ElasticsearchUtil.fromSearchHit(sh.getSourceAsString(), this.objectMapper, DecisionInstanceEntity.class);
            entity.setSortValues(sh.getSortValues());
            return entity;
         });
         if (request.getSearchBefore() != null) {
            Collections.reverse(decisionInstanceEntities);
         }

         return decisionInstanceEntities;
      } catch (IOException var8) {
         String message = String.format("Exception occurred, while obtaining instances list: %s", var8.getMessage());
         logger.error(message, var8);
         throw new OperateRuntimeException(message, var8);
      }
   }

   private void applySorting(SearchSourceBuilder searchSourceBuilder, DecisionInstanceListRequestDto request) {
      String sortBy = this.getSortBy(request);
      boolean directSorting = request.getSearchAfter() != null || request.getSearchBefore() == null;
      if (request.getSorting() != null) {
         SortOrder sort1DirectOrder = SortOrder.fromString(request.getSorting().getSortOrder());
         FieldSortBuilder sort1;
         if (directSorting) {
            sort1 = ((FieldSortBuilder)SortBuilders.fieldSort(sortBy).order(sort1DirectOrder)).missing("_last");
         } else {
            sort1 = ((FieldSortBuilder)SortBuilders.fieldSort(sortBy).order(this.reverseOrder(sort1DirectOrder))).missing("_first");
         }

         searchSourceBuilder.sort(sort1);
      }

      Object[] querySearchAfter;
      SortBuilder sort2;
      SortBuilder sort3;
      if (directSorting) {
         sort2 = SortBuilders.fieldSort("key").order(SortOrder.ASC);
         sort3 = SortBuilders.fieldSort("executionIndex").order(SortOrder.ASC);
         querySearchAfter = request.getSearchAfter();
      } else {
         sort2 = SortBuilders.fieldSort("key").order(SortOrder.DESC);
         sort3 = SortBuilders.fieldSort("executionIndex").order(SortOrder.DESC);
         querySearchAfter = request.getSearchBefore();
      }

      searchSourceBuilder.sort(sort2).sort(sort3).size(request.getPageSize());
      if (querySearchAfter != null) {
         searchSourceBuilder.searchAfter(querySearchAfter);
      }

   }

   private String getSortBy(DecisionInstanceListRequestDto request) {
      if (request.getSorting() != null) {
         String sortBy = request.getSorting().getSortBy();
         if (sortBy.equals("id")) {
            sortBy = "key";
         } else if (sortBy.equals("processInstanceId")) {
            sortBy = "processInstanceKey";
         }

         return sortBy;
      } else {
         return null;
      }
   }

   private SortOrder reverseOrder(SortOrder sortOrder) {
      return sortOrder.equals(SortOrder.ASC) ? SortOrder.DESC : SortOrder.ASC;
   }

   private QueryBuilder createRequestQuery(DecisionInstanceListQueryDto query) {
      QueryBuilder queryBuilder = ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{this.createEvaluatedFailedQuery(query), this.createDecisionDefinitionIdsQuery(query), this.createIdsQuery(query), this.createProcessInstanceIdQuery(query), this.createEvaluationDateQuery(query)});
      if (queryBuilder == null) {
         queryBuilder = QueryBuilders.matchAllQuery();
      }

      return (QueryBuilder)queryBuilder;
   }

   private QueryBuilder createEvaluationDateQuery(DecisionInstanceListQueryDto query) {
      if (query.getEvaluationDateAfter() == null && query.getEvaluationDateBefore() == null) {
         return null;
      } else {
         RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("evaluationDate");
         if (query.getEvaluationDateAfter() != null) {
            rangeQueryBuilder.gte(this.dateTimeFormatter.format(query.getEvaluationDateAfter()));
         }

         if (query.getEvaluationDateBefore() != null) {
            rangeQueryBuilder.lt(this.dateTimeFormatter.format(query.getEvaluationDateBefore()));
         }

         rangeQueryBuilder.format(this.operateProperties.getElasticsearch().getElsDateFormat());
         return rangeQueryBuilder;
      }
   }

   private QueryBuilder createProcessInstanceIdQuery(DecisionInstanceListQueryDto query) {
      return query.getProcessInstanceId() != null ? QueryBuilders.termQuery("processInstanceKey", query.getProcessInstanceId()) : null;
   }

   private QueryBuilder createIdsQuery(DecisionInstanceListQueryDto query) {
      return CollectionUtil.isNotEmpty(query.getIds()) ? QueryBuilders.termsQuery("id", query.getIds()) : null;
   }

   private QueryBuilder createDecisionDefinitionIdsQuery(DecisionInstanceListQueryDto query) {
      return CollectionUtil.isNotEmpty(query.getDecisionDefinitionIds()) ? QueryBuilders.termsQuery("decisionDefinitionId", query.getDecisionDefinitionIds()) : null;
   }

   private QueryBuilder createEvaluatedFailedQuery(DecisionInstanceListQueryDto query) {
      if (query.isEvaluated() && query.isFailed()) {
         return null;
      } else if (query.isFailed()) {
         return QueryBuilders.termQuery("state", DecisionInstanceState.FAILED);
      } else {
         return (QueryBuilder)(query.isEvaluated() ? QueryBuilders.termQuery("state", DecisionInstanceState.EVALUATED) : ElasticsearchUtil.createMatchNoneQuery());
      }
   }

   public Map getDecisionInstanceDRDData(String decisionInstanceId) {
      Long decisionInstanceKey = DecisionInstanceEntity.extractKey(decisionInstanceId);
      SearchRequest request = ElasticsearchUtil.createSearchRequest(this.decisionInstanceTemplate).source((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", decisionInstanceKey)).fetchSource(new String[]{"decisionId", "state"}, (String[])null).sort("evaluationDate", SortOrder.ASC));

      try {
         List entries = ElasticsearchUtil.scroll(request, DRDDataEntryDto.class, this.objectMapper, this.esClient, (sh) -> {
            Map map = sh.getSourceAsMap();
            return new DRDDataEntryDto(sh.getId(), (String)map.get("decisionId"), DecisionInstanceState.valueOf((String)map.get("state")));
         }, (Consumer)null, (Consumer)null);
         return (Map)entries.stream().collect(Collectors.groupingBy(DRDDataEntryDto::getDecisionId));
      } catch (IOException var5) {
         throw new OperateRuntimeException("Exception occurred while quiering DRD data for decision instance id: " + decisionInstanceId);
      }
   }
}
