package io.camunda.operate.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.schema.templates.AbstractTemplateDescriptor;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.GetTaskRequest;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.tasks.RawTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ElasticsearchUtil {
   private static final Logger logger = LoggerFactory.getLogger(ElasticsearchUtil.class);
   public static final String ZEEBE_INDEX_DELIMITER = "_";
   public static final int SCROLL_KEEP_ALIVE_MS = 60000;
   public static final int INTERNAL_SCROLL_KEEP_ALIVE_MS = 30000;
   public static final int TERMS_AGG_SIZE = 10000;
   public static final int QUERY_MAX_SIZE = 10000;
   public static final int TOPHITS_AGG_SIZE = 100;
   public static final int UPDATE_RETRY_COUNT = 3;
   public static final String TASKS_INDEX_NAME = ".tasks";
   public static final Function<SearchHit, Long> searchHitIdToLong = (hit) -> {
      return Long.valueOf(hit.getId());
   };
   public static final Function<SearchHit, String> searchHitIdToString = SearchHit::getId;

   public static long reindex(ReindexRequest reindexRequest, String sourceIndexName, RestHighLevelClient esClient) throws IOException {
      String taskId = esClient.submitReindexTask(reindexRequest, RequestOptions.DEFAULT).getTask();
      logger.debug("Reindexing started for index {}. Task id: {}", sourceIndexName, taskId);
      return waitAndCheckTaskResult(taskId, sourceIndexName, "reindex", esClient);
   }

   public static long waitAndCheckTaskResult(String taskId, String sourceIndexName, String operation, RestHighLevelClient esClient) throws IOException {
      String[] taskIdParts = taskId.split(":");
      String nodeId = taskIdParts[0];
      long smallTaskId = Long.parseLong(taskIdParts[1]);
      boolean finished = false;
      RawTaskStatus status = null;

      while(!finished) {
         GetTaskRequest getTaskRequest = new GetTaskRequest(nodeId, smallTaskId);
         Optional getTaskResponseOptional = esClient.tasks().get(getTaskRequest, RequestOptions.DEFAULT);
         if (getTaskResponseOptional.isEmpty()) {
            throw new OperateRuntimeException("Task was not found: " + taskId);
         }

         if (!((GetTaskResponse)getTaskResponseOptional.get()).isCompleted()) {
            ThreadUtil.sleepFor(2000L);
         } else {
            GetTaskResponse getTaskResponse = (GetTaskResponse)getTaskResponseOptional.get();
            status = (RawTaskStatus)getTaskResponse.getTaskInfo().getStatus();
            finished = true;
         }
      }

      Map statusMap = status.toMap();
      long total = (long)(Integer)statusMap.get("total");
      long created = (long)(Integer)statusMap.get("created");
      long updated = (long)(Integer)statusMap.get("updated");
      long deleted = (long)(Integer)statusMap.get("deleted");
      if (created + updated + deleted < total) {
         String errorMsg = String.format("Failures occurred when performing operation %s on source index %s. Check Elasticsearch logs.", operation, sourceIndexName);
         throw new OperateRuntimeException(errorMsg);
      } else {
         logger.debug("Operation {} succeeded on source index {}.", operation, sourceIndexName);
         return total;
      }
   }

   public static SearchRequest createSearchRequest(TemplateDescriptor template) {
      return createSearchRequest(template, ElasticsearchUtil.QueryType.ALL);
   }

   public static SearchRequest createSearchRequest(TemplateDescriptor template, QueryType queryType) {
      SearchRequest searchRequest = new SearchRequest(new String[]{whereToSearch(template, queryType)});
      return searchRequest;
   }

   private static String whereToSearch(TemplateDescriptor template, QueryType queryType) {
      switch (queryType) {
         case ONLY_RUNTIME:
            return template.getFullQualifiedName();
         case ALL:
         default:
            return template.getAlias();
      }
   }

   public static QueryBuilder joinWithOr(BoolQueryBuilder boolQueryBuilder, QueryBuilder... queries) {
      List notNullQueries = CollectionUtil.throwAwayNullElements(queries);
      Iterator var3 = notNullQueries.iterator();

      while(var3.hasNext()) {
         QueryBuilder query = (QueryBuilder)var3.next();
         boolQueryBuilder.should(query);
      }

      return boolQueryBuilder;
   }

   public static QueryBuilder joinWithOr(QueryBuilder... queries) {
      List notNullQueries = CollectionUtil.throwAwayNullElements(queries);
      switch (notNullQueries.size()) {
         case 0:
            return null;
         case 1:
            return (QueryBuilder)notNullQueries.get(0);
         default:
            BoolQueryBuilder boolQ = QueryBuilders.boolQuery();
            Iterator var3 = notNullQueries.iterator();

            while(var3.hasNext()) {
               QueryBuilder query = (QueryBuilder)var3.next();
               boolQ.should(query);
            }

            return boolQ;
      }
   }

   public static QueryBuilder joinWithOr(Collection queries) {
      return joinWithOr((QueryBuilder[])queries.toArray(new QueryBuilder[queries.size()]));
   }

   public static QueryBuilder joinWithAnd(QueryBuilder... queries) {
      List notNullQueries = CollectionUtil.throwAwayNullElements(queries);
      switch (notNullQueries.size()) {
         case 0:
            return null;
         case 1:
            return (QueryBuilder)notNullQueries.get(0);
         default:
            BoolQueryBuilder boolQ = QueryBuilders.boolQuery();
            Iterator var3 = notNullQueries.iterator();

            while(var3.hasNext()) {
               QueryBuilder query = (QueryBuilder)var3.next();
               boolQ.must(query);
            }

            return boolQ;
      }
   }

   public static QueryBuilder addToBoolMust(BoolQueryBuilder boolQuery, QueryBuilder... queries) {
      if (boolQuery.mustNot().size() == 0 && boolQuery.filter().size() == 0 && boolQuery.should().size() == 0) {
         List notNullQueries = CollectionUtil.throwAwayNullElements(queries);
         Iterator var3 = notNullQueries.iterator();

         while(var3.hasNext()) {
            QueryBuilder query = (QueryBuilder)var3.next();
            boolQuery.must(query);
         }

         return boolQuery;
      } else {
         throw new IllegalArgumentException("BoolQuery with only must elements is expected here.");
      }
   }

   public static BoolQueryBuilder createMatchNoneQuery() {
      return QueryBuilders.boolQuery().must(QueryBuilders.wrapperQuery("{\"match_none\": {}}"));
   }

   public static void processBulkRequest(RestHighLevelClient esClient, BulkRequest bulkRequest) throws PersistenceException {
      processBulkRequest(esClient, bulkRequest, false);
   }

   public static void processBulkRequest(RestHighLevelClient esClient, BulkRequest bulkRequest, Runnable afterBulkAction) throws PersistenceException {
      processBulkRequest(esClient, bulkRequest, false);
      afterBulkAction.run();
   }

   public static void processBulkRequest(RestHighLevelClient esClient, BulkRequest bulkRequest, boolean refreshImmediately) throws PersistenceException {
      if (bulkRequest.requests().size() > 0) {
         try {
            logger.debug("************* FLUSH BULK START *************");
            if (refreshImmediately) {
               bulkRequest = bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
            }

            BulkResponse bulkItemResponses = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            BulkItemResponse[] items = bulkItemResponses.getItems();

            for(int i = 0; i < items.length; ++i) {
               BulkItemResponse responseItem = items[i];
               if (responseItem.isFailed() && !isEventConflictError(responseItem)) {
                  if (!isMissingIncident(responseItem)) {
                     logger.error(String.format("%s failed for type [%s] and id [%s]: %s", responseItem.getOpType(), responseItem.getIndex(), responseItem.getId(), responseItem.getFailureMessage()), responseItem.getFailure().getCause());
                     throw new PersistenceException("Operation failed: " + responseItem.getFailureMessage(), responseItem.getFailure().getCause(), responseItem.getItemId());
                  }

                  DocWriteRequest request = (DocWriteRequest)bulkRequest.requests().get(i);
                  String incidentId = extractIncidentId(responseItem.getFailure().getMessage());
                  String indexName = (String)getIndexNames((String)(request.index() + "alias"), Arrays.asList(incidentId), esClient).get(incidentId);
                  request.index(indexName);
                  if (indexName == null) {
                     logger.warn("Index is not known for incident: " + incidentId);
                  }

                  esClient.update((UpdateRequest)request, RequestOptions.DEFAULT);
               }
            }

            logger.debug("************* FLUSH BULK FINISH *************");
         } catch (IOException var10) {
            throw new PersistenceException("Error when processing bulk request against Elasticsearch: " + var10.getMessage(), var10);
         }
      }

   }

   public static String extractIncidentId(String errorMessage) {
      Pattern fniPattern = Pattern.compile(".*\\[_doc\\]\\[(\\d*)\\].*");
      Matcher matcher = fniPattern.matcher(errorMessage);
      matcher.matches();
      return matcher.group(1);
   }

   private static boolean isMissingIncident(BulkItemResponse responseItem) {
      return responseItem.getIndex().contains("incident") && responseItem.getFailure().getStatus().equals(RestStatus.NOT_FOUND);
   }

   private static boolean isEventConflictError(BulkItemResponse responseItem) {
      return responseItem.getIndex().contains("event") && responseItem.getFailure().getStatus().equals(RestStatus.CONFLICT);
   }

   public static void executeUpdate(RestHighLevelClient esClient, UpdateRequest updateRequest) throws PersistenceException {
      try {
         esClient.update(updateRequest, RequestOptions.DEFAULT);
      } catch (IOException | ElasticsearchException var4) {
         String errorMessage = String.format("Update request failed for [%s] and id [%s] with the message [%s].", updateRequest.index(), updateRequest.id(), var4.getMessage());
         logger.error(errorMessage, var4);
         throw new PersistenceException(errorMessage, var4);
      }
   }

   public static List mapSearchHits(List searchHits, ObjectMapper objectMapper, JavaType valueType) {
      return mapSearchHits((SearchHit[])searchHits.toArray(new SearchHit[searchHits.size()]), objectMapper, valueType);
   }

   public static List mapSearchHits(SearchHit[] searchHits, Function searchHitMapper) {
      return CollectionUtil.map(searchHits, searchHitMapper);
   }

   public static List mapSearchHits(SearchHit[] searchHits, ObjectMapper objectMapper, Class clazz) {
      return CollectionUtil.map(searchHits, (searchHit) -> {
         return fromSearchHit(searchHit.getSourceAsString(), objectMapper, clazz);
      });
   }

   public static Object fromSearchHit(String searchHitString, ObjectMapper objectMapper, Class clazz) {
      try {
         Object entity = objectMapper.readValue(searchHitString, clazz);
         return entity;
      } catch (IOException var5) {
         logger.error(String.format("Error while reading entity of type %s from Elasticsearch!", clazz.getName()), var5);
         throw new OperateRuntimeException(String.format("Error while reading entity of type %s from Elasticsearch!", clazz.getName()), var5);
      }
   }

   public static List mapSearchHits(SearchHit[] searchHits, ObjectMapper objectMapper, JavaType valueType) {
      return CollectionUtil.map(searchHits, (searchHit) -> {
         return fromSearchHit(searchHit.getSourceAsString(), objectMapper, valueType);
      });
   }

   public static Object fromSearchHit(String searchHitString, ObjectMapper objectMapper, JavaType valueType) {
      try {
         Object entity = objectMapper.readValue(searchHitString, valueType);
         return entity;
      } catch (IOException var5) {
         logger.error(String.format("Error while reading entity of type %s from Elasticsearch!", valueType.toString()), var5);
         throw new OperateRuntimeException(String.format("Error while reading entity of type %s from Elasticsearch!", valueType.toString()), var5);
      }
   }

   public static List scroll(SearchRequest searchRequest, Class clazz, ObjectMapper objectMapper, RestHighLevelClient esClient) throws IOException {
      return scroll(searchRequest, clazz, objectMapper, esClient, (Consumer)null, (Consumer)null);
   }

   public static List scroll(SearchRequest searchRequest, Class clazz, ObjectMapper objectMapper, RestHighLevelClient esClient, Consumer searchHitsProcessor, Consumer aggsProcessor) throws IOException {
      return scroll(searchRequest, clazz, objectMapper, esClient, (Function)null, searchHitsProcessor, aggsProcessor);
   }

   public static List scroll(SearchRequest searchRequest, Class clazz, ObjectMapper objectMapper, RestHighLevelClient esClient, Function searchHitMapper, Consumer searchHitsProcessor, Consumer aggsProcessor) throws IOException {
      searchRequest.scroll(TimeValue.timeValueMillis(60000L));
      SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
      if (aggsProcessor != null) {
         aggsProcessor.accept(response.getAggregations());
      }

      List result = new ArrayList();
      String scrollId = response.getScrollId();

      for(SearchHits hits = response.getHits(); hits.getHits().length != 0; hits = response.getHits()) {
         if (searchHitMapper != null) {
            result.addAll(mapSearchHits(hits.getHits(), searchHitMapper));
         } else {
            result.addAll(mapSearchHits(hits.getHits(), objectMapper, clazz));
         }

         if (searchHitsProcessor != null) {
            searchHitsProcessor.accept(response.getHits());
         }

         SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
         scrollRequest.scroll(TimeValue.timeValueMillis(60000L));
         response = esClient.scroll(scrollRequest, RequestOptions.DEFAULT);
         scrollId = response.getScrollId();
      }

      clearScroll(scrollId, esClient);
      return result;
   }

   public static void scrollWith(SearchRequest searchRequest, RestHighLevelClient esClient, Consumer<SearchHits> searchHitsProcessor) throws IOException {
      scrollWith(searchRequest, esClient, searchHitsProcessor, null, null);
   }

   public static void scrollWith(SearchRequest searchRequest, RestHighLevelClient esClient, Consumer<SearchHits> searchHitsProcessor, Consumer<Aggregations> aggsProcessor, Consumer<SearchHits> firstResponseConsumer) throws IOException {
      searchRequest.scroll(TimeValue.timeValueMillis(60000L));
      SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
      if (firstResponseConsumer != null) {
         firstResponseConsumer.accept(response.getHits());
      }

      if (aggsProcessor != null) {
         aggsProcessor.accept(response.getAggregations());
      }

      String scrollId = response.getScrollId();

      for(SearchHits hits = response.getHits(); hits.getHits().length != 0; hits = response.getHits()) {
         if (searchHitsProcessor != null) {
            searchHitsProcessor.accept(response.getHits());
         }

         SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
         scrollRequest.scroll(TimeValue.timeValueMillis(60000L));
         response = esClient.scroll(scrollRequest, RequestOptions.DEFAULT);
         scrollId = response.getScrollId();
      }

      clearScroll(scrollId, esClient);
   }

   private static void clearScroll(String scrollId, RestHighLevelClient esClient) {
      if (scrollId != null) {
         ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
         clearScrollRequest.addScrollId(scrollId);

         try {
            esClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
         } catch (Exception var4) {
            logger.warn("Error occurred when clearing the scroll with id [{}]", scrollId);
         }
      }

   }

   public static List<String> scrollIdsToList(SearchRequest request, RestHighLevelClient esClient) throws IOException {
      List<String> result = new ArrayList<>();
      Consumer<SearchHits> collectIds = (hits) -> {
         result.addAll(CollectionUtil.map(hits.getHits(), searchHitIdToString));
      };
      scrollWith(request, esClient, collectIds, null, collectIds);
      return result;
   }

   public static List<Long> scrollKeysToList(SearchRequest request, RestHighLevelClient esClient) throws IOException {
      List<Long> result = new ArrayList<>();
      Consumer<SearchHits> collectIds = (hits) -> {
         result.addAll(CollectionUtil.map(hits.getHits(), searchHitIdToLong));
      };
      scrollWith(request, esClient, collectIds, null, collectIds);
      return result;
   }

   public static List scrollFieldToList(SearchRequest request, String fieldName, RestHighLevelClient esClient) throws IOException {
      List result = new ArrayList();
      Function<SearchHit, Object> searchHitFieldToString = (searchHit) -> {
         return searchHit.getSourceAsMap().get(fieldName);
      };
      Consumer<SearchHits> collectFields = (hits) -> {
         result.addAll(CollectionUtil.map(hits.getHits(), searchHitFieldToString));
      };
      scrollWith(request, esClient, collectFields, (Consumer)null, collectFields);
      return result;
   }

   public static Set scrollIdsToSet(SearchRequest request, RestHighLevelClient esClient) throws IOException {
      Set result = new HashSet();
      Consumer<SearchHits> collectIds = (hits) -> {
         result.addAll(CollectionUtil.map(hits.getHits(), searchHitIdToString));
      };
      scrollWith(request, esClient, collectIds, (Consumer)null, collectIds);
      return result;
   }

   public static Set scrollKeysToSet(SearchRequest request, RestHighLevelClient esClient) throws IOException {
      Set result = new HashSet();
      Consumer<SearchHits> collectIds = (hits) -> {
         result.addAll(CollectionUtil.map(hits.getHits(), searchHitIdToLong));
      };
      scrollWith(request, esClient, collectIds, (Consumer)null, collectIds);
      return result;
   }

   public static Map getIndexNames(String aliasName, Collection ids, RestHighLevelClient esClient) {
      Map indexNames = new HashMap();
      SearchRequest piRequest = (new SearchRequest(new String[]{aliasName})).source((new SearchSourceBuilder()).query(QueryBuilders.idsQuery().addIds((String[])ids.toArray((x$0) -> {
         return new String[x$0];
      }))).fetchSource(false));

      try {
         scrollWith(piRequest, esClient, (sh) -> {
            indexNames.putAll((Map)Arrays.stream(sh.getHits()).collect(Collectors.toMap((hit) -> {
               return hit.getId();
            }, (hit) -> {
               return hit.getIndex();
            })));
         });
         return indexNames;
      } catch (IOException var6) {
         throw new OperateRuntimeException(var6.getMessage(), var6);
      }
   }

   public static Map getIndexNames(AbstractTemplateDescriptor template, Collection ids, RestHighLevelClient esClient) {
      Map indexNames = new HashMap();
      SearchRequest piRequest = createSearchRequest(template).source((new SearchSourceBuilder()).query(QueryBuilders.idsQuery().addIds((String[])ids.toArray((x$0) -> {
         return new String[x$0];
      }))).fetchSource(false));

      try {
         scrollWith(piRequest, esClient, (sh) -> {
            indexNames.putAll((Map)Arrays.stream(sh.getHits()).collect(Collectors.toMap((hit) -> {
               return hit.getId();
            }, (hit) -> {
               return hit.getIndex();
            })));
         });
         return indexNames;
      } catch (IOException var6) {
         throw new OperateRuntimeException(var6.getMessage(), var6);
      }
   }

   public static Map<String, List<String>> getIndexNamesAsList(AbstractTemplateDescriptor template, Collection ids, RestHighLevelClient esClient) {
      Map<String, List<String>> indexNames = new ConcurrentHashMap<>();
      SearchRequest piRequest = createSearchRequest(template).source((new SearchSourceBuilder()).query(QueryBuilders.idsQuery().addIds((String[])ids.toArray((x$0) -> {
         return new String[x$0];
      }))).fetchSource(false));

      try {
         scrollWith(piRequest, esClient, (sh) -> {
            (Arrays.stream(sh.getHits()).collect(Collectors.groupingBy(SearchHit::getId, Collectors.mapping(SearchHit::getIndex, Collectors.toList())))).forEach((key, value) -> {
               indexNames.merge(key, value, (v1, v2) -> {
                  v1.addAll(v2);
                  return v1;
               });
            });
         });
         return indexNames;
      } catch (IOException var6) {
         throw new OperateRuntimeException(var6.getMessage(), var6);
      }
   }

   public static enum QueryType {
      ONLY_RUNTIME,
      ALL;
   }
}
