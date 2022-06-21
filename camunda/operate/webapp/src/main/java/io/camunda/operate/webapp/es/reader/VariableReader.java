package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.VariableEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.templates.VariableTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
import io.camunda.operate.webapp.rest.dto.VariableDto;
import io.camunda.operate.webapp.rest.dto.VariableRequestDto;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VariableReader extends AbstractReader {
   private static final Logger logger = LoggerFactory.getLogger(VariableReader.class);
   @Autowired
   private VariableTemplate variableTemplate;
   @Autowired
   private OperationReader operationReader;
   @Autowired
   private OperateProperties operateProperties;

   public List getVariables(String processInstanceId, VariableRequestDto request) {
      List response = this.queryVariables(processInstanceId, request);
      if (request.getSearchAfterOrEqual() != null || request.getSearchBeforeOrEqual() != null) {
         this.adjustResponse(response, processInstanceId, request);
      }

      if (response.size() > 0 && (request.getSearchAfter() != null || request.getSearchAfterOrEqual() != null)) {
         VariableDto firstVar = (VariableDto)response.get(0);
         firstVar.setIsFirst(this.checkVarIsFirst(processInstanceId, request, firstVar.getId()));
      }

      return response;
   }

   private boolean checkVarIsFirst(String processInstanceId, VariableRequestDto query, String id) {
      VariableRequestDto newQuery = (VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)query.createCopy().setSearchAfter((Object[])null)).setSearchAfterOrEqual((Object[])null)).setSearchBefore((Object[])null)).setSearchBeforeOrEqual((Object[])null)).setPageSize(1);
      List vars = this.queryVariables(processInstanceId, newQuery);
      return vars.size() > 0 ? ((VariableDto)vars.get(0)).getId().equals(id) : false;
   }

   private void adjustResponse(List response, String processInstanceId, VariableRequestDto request) {
      String variableName = null;
      if (request.getSearchAfterOrEqual() != null) {
         variableName = (String)request.getSearchAfterOrEqual()[0];
      } else if (request.getSearchBeforeOrEqual() != null) {
         variableName = (String)request.getSearchBeforeOrEqual()[0];
      }

      VariableRequestDto newRequest = (VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)request.createCopy().setSearchAfter((Object[])null)).setSearchAfterOrEqual((Object[])null)).setSearchBefore((Object[])null)).setSearchBeforeOrEqual((Object[])null);
      List entities = this.queryVariables(processInstanceId, newRequest, variableName);
      if (entities.size() > 0) {
         VariableDto entity = (VariableDto)entities.get(0);
         entity.setIsFirst(false);
         if (request.getSearchAfterOrEqual() != null) {
            if (request.getPageSize() != null && response.size() == request.getPageSize()) {
               response.remove(response.size() - 1);
            }

            response.add(0, entity);
         } else if (request.getSearchBeforeOrEqual() != null) {
            if (request.getPageSize() != null && response.size() == request.getPageSize()) {
               response.remove(0);
            }

            response.add(entity);
         }
      }

   }

   private List queryVariables(String processInstanceId, VariableRequestDto variableRequest) {
      return this.queryVariables(processInstanceId, variableRequest, (String)null);
   }

   private List queryVariables(String processInstanceId, VariableRequestDto request, String varName) {
      Long scopeKey = null;
      if (request.getScopeId() != null) {
         scopeKey = Long.valueOf(request.getScopeId());
      }

      TermQueryBuilder processInstanceKeyQuery = QueryBuilders.termQuery("processInstanceKey", processInstanceId);
      TermQueryBuilder scopeKeyQuery = QueryBuilders.termQuery("scopeKey", scopeKey);
      TermQueryBuilder varNameQ = null;
      if (varName != null) {
         varNameQ = QueryBuilders.termQuery("name", varName);
      }

      ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceKeyQuery, scopeKeyQuery, varNameQ}));
      SearchSourceBuilder searchSourceBuilder = (new SearchSourceBuilder()).query(query).fetchSource((String)null, "fullValue");
      this.applySorting(searchSourceBuilder, request);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.variableTemplate, QueryType.ALL).source(searchSourceBuilder);

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         List variableEntities = ElasticsearchUtil.mapSearchHits(response.getHits().getHits(), (sh) -> {
            VariableEntity entity = (VariableEntity)ElasticsearchUtil.fromSearchHit(sh.getSourceAsString(), this.objectMapper, VariableEntity.class);
            entity.setSortValues(sh.getSortValues());
            return entity;
         });
         Map operations = this.operationReader.getUpdateOperationsPerVariableName(Long.valueOf(processInstanceId), scopeKey);
         List variables = VariableDto.createFrom(variableEntities, operations, this.operateProperties.getImporter().getVariableSizeThreshold());
         if (variables.size() > 0) {
            if (request.getSearchBefore() == null && request.getSearchBeforeOrEqual() == null) {
               if (request.getSearchAfter() == null && request.getSearchAfterOrEqual() == null) {
                  ((VariableDto)variables.get(0)).setIsFirst(true);
               }
            } else {
               if (variables.size() <= request.getPageSize()) {
                  ((VariableDto)variables.get(variables.size() - 1)).setIsFirst(true);
               } else {
                  variables.remove(variables.size() - 1);
               }

               Collections.reverse(variables);
            }
         }

         return variables;
      } catch (IOException var15) {
         String message = String.format("Exception occurred, while obtaining variables: %s", var15.getMessage());
         throw new OperateRuntimeException(message, var15);
      }
   }

   private void applySorting(SearchSourceBuilder searchSourceBuilder, VariableRequestDto request) {
      boolean directSorting = request.getSearchAfter() != null || request.getSearchAfterOrEqual() != null || request.getSearchBefore() == null && request.getSearchBeforeOrEqual() == null;
      if (directSorting) {
         searchSourceBuilder.sort("name", SortOrder.ASC);
         if (request.getSearchAfter() != null) {
            searchSourceBuilder.searchAfter(request.getSearchAfter());
         } else if (request.getSearchAfterOrEqual() != null) {
            searchSourceBuilder.searchAfter(request.getSearchAfterOrEqual());
         }

         searchSourceBuilder.size(request.getPageSize());
      } else {
         searchSourceBuilder.sort("name", SortOrder.DESC);
         if (request.getSearchBefore() != null) {
            searchSourceBuilder.searchAfter(request.getSearchBefore());
         } else if (request.getSearchBeforeOrEqual() != null) {
            searchSourceBuilder.searchAfter(request.getSearchBeforeOrEqual());
         }

         searchSourceBuilder.size(request.getPageSize() + 1);
      }

   }

   public VariableDto getVariable(String id) {
      IdsQueryBuilder idsQ = QueryBuilders.idsQuery().addIds(new String[]{id});
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.variableTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(idsQ));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value != 1L) {
            throw new NotFoundException(String.format("Variable with id %s not found.", id));
         } else {
            VariableEntity variableEntity = (VariableEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getHits()[0].getSourceAsString(), this.objectMapper, VariableEntity.class);
            return VariableDto.createFrom(variableEntity, (List)null, true, this.operateProperties.getImporter().getVariableSizeThreshold());
         }
      } catch (IOException var6) {
         String message = String.format("Exception occurred, while obtaining variable: %s", var6.getMessage());
         logger.error(message, var6);
         throw new OperateRuntimeException(message, var6);
      }
   }

   public VariableDto getVariableByName(String processInstanceId, String scopeId, String variableName) {
      TermQueryBuilder processInstanceIdQ = QueryBuilders.termQuery("processInstanceKey", processInstanceId);
      TermQueryBuilder scopeIdQ = QueryBuilders.termQuery("scopeKey", scopeId);
      TermQueryBuilder varNameQ = QueryBuilders.termQuery("name", variableName);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.variableTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(QueryBuilders.constantScoreQuery(ElasticsearchUtil.joinWithAnd(new QueryBuilder[]{processInstanceIdQ, scopeIdQ, varNameQ}))));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value > 0L) {
            VariableEntity variableEntity = (VariableEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getHits()[0].getSourceAsString(), this.objectMapper, VariableEntity.class);
            return VariableDto.createFrom(variableEntity, (List)null, true, this.operateProperties.getImporter().getVariableSizeThreshold());
         } else {
            return null;
         }
      } catch (IOException var10) {
         String message = String.format("Exception occurred, while obtaining variable for processInstanceId: %s, scopeId: %s, name: %s, error: %s", processInstanceId, scopeId, variableName, var10.getMessage());
         throw new OperateRuntimeException(message, var10);
      }
   }
}
