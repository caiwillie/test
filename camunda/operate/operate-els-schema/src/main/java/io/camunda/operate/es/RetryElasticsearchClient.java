package io.camunda.operate.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.RetryOperation;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.ingest.DeletePipelineRequest;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.ComposableIndexTemplateExistRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.DeleteComposableIndexTemplateRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.client.indices.PutComponentTemplateRequest;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.client.tasks.GetTaskRequest;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.tasks.RawTaskStatus;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RetryElasticsearchClient {
   public static final String REFRESH_INTERVAL = "index.refresh_interval";
   public static final String NO_REFRESH = "-1";
   public static final String NUMBERS_OF_REPLICA = "index.number_of_replicas";
   public static final String NO_REPLICA = "0";
   private static final Logger logger = LoggerFactory.getLogger(RetryElasticsearchClient.class);
   public static final int SCROLL_KEEP_ALIVE_MS = 60000;
   public static final int DEFAULT_NUMBER_OF_RETRIES = 300;
   public static final int DEFAULT_DELAY_INTERVAL_IN_SECONDS = 2;
   @Autowired
   private RestHighLevelClient esClient;
   private RequestOptions requestOptions;
   private int numberOfRetries;
   private int delayIntervalInSeconds;

   public RetryElasticsearchClient() {
      this.requestOptions = RequestOptions.DEFAULT;
      this.numberOfRetries = 300;
      this.delayIntervalInSeconds = 2;
   }

   public int getNumberOfRetries() {
      return this.numberOfRetries;
   }

   public RetryElasticsearchClient setNumberOfRetries(int numberOfRetries) {
      this.numberOfRetries = numberOfRetries;
      return this;
   }

   public int getDelayIntervalInSeconds() {
      return this.delayIntervalInSeconds;
   }

   public RetryElasticsearchClient setDelayIntervalInSeconds(int delayIntervalInSeconds) {
      this.delayIntervalInSeconds = delayIntervalInSeconds;
      return this;
   }

   public RetryElasticsearchClient setRequestOptions(RequestOptions requestOptions) {
      this.requestOptions = requestOptions;
      return this;
   }

   public void refresh(String indexPattern) {
      this.executeWithRetries("Refresh " + indexPattern, () -> {
         return this.esClient.indices().refresh(new RefreshRequest(new String[]{indexPattern}), this.requestOptions);
      });
   }

   public long getNumberOfDocumentsFor(String... indexPatterns) {
      return (Long)this.executeWithRetries("Count number of documents in " + Arrays.asList(indexPatterns), () -> {
         return this.esClient.count(new CountRequest(indexPatterns), this.requestOptions).getCount();
      });
   }

   public Set getIndexNames(String namePattern) {
      return (Set)this.executeWithRetries("Get indices for " + namePattern, () -> {
         try {
            GetIndexResponse response = this.esClient.indices().get(new GetIndexRequest(new String[]{namePattern}), RequestOptions.DEFAULT);
            return Set.of(response.getIndices());
         } catch (ElasticsearchException var3) {
            if (var3.status().equals(RestStatus.NOT_FOUND)) {
               return Set.of();
            } else {
               throw var3;
            }
         }
      });
   }

   public boolean createIndex(CreateIndexRequest createIndexRequest) {
      return (Boolean)this.executeWithRetries("CreateIndex " + createIndexRequest.index(), () -> {
         return !this.indicesExist(createIndexRequest.index()) ? this.esClient.indices().create(createIndexRequest, this.requestOptions).isAcknowledged() : true;
      });
   }

   public boolean createOrUpdateDocument(String name, String id, Map source) {
      return (Boolean)this.executeWithRetries(() -> {
         IndexResponse response = this.esClient.index((new IndexRequest(name)).id(id).source(source, XContentType.JSON), this.requestOptions);
         DocWriteResponse.Result result = response.getResult();
         return result.equals(Result.CREATED) || result.equals(Result.UPDATED);
      });
   }

   public boolean createOrUpdateDocument(String name, String id, String source) {
      return (Boolean)this.executeWithRetries(() -> {
         IndexResponse response = this.esClient.index((new IndexRequest(name)).id(id).source(source, XContentType.JSON), this.requestOptions);
         DocWriteResponse.Result result = response.getResult();
         return result.equals(Result.CREATED) || result.equals(Result.UPDATED);
      });
   }

   public boolean documentExists(String name, String id) {
      return (Boolean)this.executeWithGivenRetries(10, String.format("Exists document from %s with id %s", name, id), () -> {
         return this.esClient.exists((new GetRequest(name)).id(id), this.requestOptions);
      }, (RetryOperation.RetryPredicate)null);
   }

   public Map getDocument(String name, String id) {
      return (Map)this.executeWithGivenRetries(10, String.format("Get document from %s with id %s", name, id), () -> {
         GetRequest request = (new GetRequest(name)).id(id);
         if (this.esClient.exists(request, this.requestOptions)) {
            GetResponse response = this.esClient.get(request, this.requestOptions);
            return response.getSourceAsMap();
         } else {
            return null;
         }
      }, (RetryOperation.RetryPredicate)null);
   }

   public boolean deleteDocument(String name, String id) {
      return (Boolean)this.executeWithRetries(() -> {
         DeleteResponse response = this.esClient.delete((new DeleteRequest(name)).id(id), this.requestOptions);
         DocWriteResponse.Result result = response.getResult();
         return result.equals(Result.DELETED);
      });
   }

   private boolean templatesExist(String templatePattern) throws IOException {
      return this.esClient.indices().existsIndexTemplate(new ComposableIndexTemplateExistRequest(templatePattern), this.requestOptions);
   }

   public boolean createComponentTemplate(PutComponentTemplateRequest request) {
      return (Boolean)this.executeWithRetries("CreateComponentTemplate " + request.name(), () -> {
         return !this.templatesExist(request.name()) ? this.esClient.cluster().putComponentTemplate(request, this.requestOptions).isAcknowledged() : true;
      });
   }

   public boolean createTemplate(PutComposableIndexTemplateRequest request) {
      return (Boolean)this.executeWithRetries("CreateTemplate " + request.name(), () -> {
         return !this.templatesExist(request.name()) ? this.esClient.indices().putIndexTemplate(request, this.requestOptions).isAcknowledged() : true;
      });
   }

   public boolean deleteTemplatesFor(String templateNamePattern) {
      return (Boolean)this.executeWithRetries("DeleteTemplate " + templateNamePattern, () -> {
         return this.templatesExist(templateNamePattern) ? this.esClient.indices().deleteIndexTemplate(new DeleteComposableIndexTemplateRequest(templateNamePattern), this.requestOptions).isAcknowledged() : true;
      });
   }

   private boolean indicesExist(String indexPattern) throws IOException {
      return this.esClient.indices().exists((new GetIndexRequest(new String[]{indexPattern})).indicesOptions(IndicesOptions.fromOptions(true, false, true, false)), this.requestOptions);
   }

   public boolean deleteIndicesFor(String indexPattern) {
      return (Boolean)this.executeWithRetries("DeleteIndices " + indexPattern, () -> {
         return this.indicesExist(indexPattern) ? this.esClient.indices().delete(new DeleteIndexRequest(indexPattern), this.requestOptions).isAcknowledged() : true;
      });
   }

   protected Map getIndexSettingsFor(String indexName, String... fields) {
      return (Map)this.executeWithRetries("GetIndexSettings " + indexName, () -> {
         Map settings = new HashMap();
         GetSettingsResponse response = this.esClient.indices().getSettings((new GetSettingsRequest()).indices(new String[]{indexName}), this.requestOptions);
         String[] var5 = fields;
         int var6 = fields.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String field = var5[var7];
            settings.put(field, response.getSetting(indexName, field));
         }

         return settings;
      });
   }

   public String getOrDefaultRefreshInterval(String indexName, String defaultValue) {
      Map settings = this.getIndexSettingsFor(indexName, "index.refresh_interval");
      String refreshInterval = (String)CollectionUtil.getOrDefaultForNullValue(settings, "index.refresh_interval", defaultValue);
      if (refreshInterval.trim().equals("-1")) {
         refreshInterval = defaultValue;
      }

      return refreshInterval;
   }

   public String getOrDefaultNumbersOfReplica(String indexName, String defaultValue) {
      Map settings = this.getIndexSettingsFor(indexName, "index.number_of_replicas");
      String numbersOfReplica = (String)CollectionUtil.getOrDefaultForNullValue(settings, "index.number_of_replicas", defaultValue);
      if (numbersOfReplica.trim().equals("0")) {
         numbersOfReplica = defaultValue;
      }

      return numbersOfReplica;
   }

   public boolean setIndexSettingsFor(Settings settings, String indexPattern) {
      return (Boolean)this.executeWithRetries("SetIndexSettings " + indexPattern, () -> {
         return this.esClient.indices().putSettings((new UpdateSettingsRequest(new String[]{indexPattern})).settings(settings), this.requestOptions).isAcknowledged();
      });
   }

   public boolean addPipeline(String name, String definition) {
      BytesReference content = new BytesArray(definition.getBytes());
      return (Boolean)this.executeWithRetries("AddPipeline " + name, () -> {
         return this.esClient.ingest().putPipeline(new PutPipelineRequest(name, content, XContentType.JSON), this.requestOptions).isAcknowledged();
      });
   }

   public boolean removePipeline(String name) {
      return (Boolean)this.executeWithRetries("RemovePipeline " + name, () -> {
         return this.esClient.ingest().deletePipeline(new DeletePipelineRequest(name), this.requestOptions).isAcknowledged();
      });
   }

   public void reindex(ReindexRequest reindexRequest) {
      this.reindex(reindexRequest, true);
   }

   public void reindex(ReindexRequest reindexRequest, boolean checkDocumentCount) {
      this.executeWithRetries("Reindex " + Arrays.asList(reindexRequest.getSearchRequest().indices()) + " -> " + reindexRequest.getDestination().index(), () -> {
         String srcIndices = reindexRequest.getSearchRequest().indices()[0];
         long srcCount = this.getNumberOfDocumentsFor(srcIndices);
         String dstIndex;
         if (checkDocumentCount) {
            dstIndex = reindexRequest.getDestination().indices()[0];
            long dstCount = this.getNumberOfDocumentsFor(dstIndex + "*");
            if (srcCount == dstCount) {
               logger.info("Reindex of {} -> {} is already done.", srcIndices, dstIndex);
               return true;
            }
         }

         dstIndex = this.esClient.submitReindexTask(reindexRequest, this.requestOptions).getTask();
         TimeUnit.of(ChronoUnit.MILLIS).sleep(2000L);
         return this.waitUntilTaskIsCompleted(dstIndex, srcCount);
      }, (done) -> {
         return !done;
      });
   }

   private Map getTaskStatusMap(GetTaskResponse taskResponse) {
      return ((RawTaskStatus)taskResponse.getTaskInfo().getStatus()).toMap();
   }

   private boolean needsToPollAgain(Optional taskResponse) {
      if (taskResponse.isEmpty()) {
         return false;
      } else {
         Map statusMap = this.getTaskStatusMap((GetTaskResponse)taskResponse.get());
         long total = (long)(Integer)statusMap.get("total");
         long created = (long)(Integer)statusMap.get("created");
         long updated = (long)(Integer)statusMap.get("updated");
         long deleted = (long)(Integer)statusMap.get("deleted");
         return !((GetTaskResponse)taskResponse.get()).isCompleted() || created + updated + deleted != total;
      }
   }

   private boolean waitUntilTaskIsCompleted(String taskId, long srcCount) {
      String[] taskIdParts = taskId.split(":");
      String nodeId = taskIdParts[0];
      long smallTaskId = Long.parseLong(taskIdParts[1]);
      Optional taskResponse = (Optional)this.executeWithGivenRetries(Integer.MAX_VALUE, "GetTaskInfo{" + nodeId + "},{" + smallTaskId + "}", () -> {
         return this.esClient.tasks().get(new GetTaskRequest(nodeId, smallTaskId), this.requestOptions);
      }, this::needsToPollAgain);
      if (taskResponse.isPresent()) {
         long total = (long)(Integer)this.getTaskStatusMap((GetTaskResponse)taskResponse.get()).get("total");
         logger.info("Source docs: {}, Migrated docs: {}", srcCount, total);
         return total == srcCount;
      } else {
         return false;
      }
   }

   public int doWithEachSearchResult(SearchRequest searchRequest, Consumer searchHitConsumer) {
      return (Integer)this.executeWithRetries(() -> {
         int doneOnSearchHits = 0;
         searchRequest.scroll(TimeValue.timeValueMillis(60000L));
         SearchResponse response = this.esClient.search(searchRequest, this.requestOptions);

         String scrollId;
         SearchScrollRequest scrollRequest;
         for(scrollId = null; response.getHits().getHits().length > 0; response = this.esClient.scroll(scrollRequest, this.requestOptions)) {
            ((Stream)Arrays.stream(response.getHits().getHits()).sequential()).forEach(searchHitConsumer);
            doneOnSearchHits += response.getHits().getHits().length;
            scrollId = response.getScrollId();
            scrollRequest = (new SearchScrollRequest(scrollId)).scroll(TimeValue.timeValueMillis(60000L));
         }

         if (scrollId != null) {
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            this.esClient.clearScroll(clearScrollRequest, this.requestOptions);
         }

         return doneOnSearchHits;
      });
   }

   public List searchWithScroll(SearchRequest searchRequest, Class resultClass, ObjectMapper objectMapper) {
      long totalHits = (Long)this.executeWithRetries("Count search results", () -> {
         return this.esClient.search(searchRequest, this.requestOptions).getHits().getTotalHits().value;
      });
      return (List)this.executeWithRetries("Search with scroll", () -> {
         return this.scroll(searchRequest, resultClass, objectMapper);
      }, (resultList) -> {
         return (long)resultList.size() != totalHits;
      });
   }

   private List scroll(SearchRequest searchRequest, Class clazz, ObjectMapper objectMapper) throws IOException {
      List results = new ArrayList();
      searchRequest.scroll(TimeValue.timeValueMillis(60000L));
      SearchResponse response = this.esClient.search(searchRequest, this.requestOptions);

      String scrollId;
      SearchScrollRequest scrollRequest;
      for(scrollId = null; response.getHits().getHits().length > 0; response = this.esClient.scroll(scrollRequest, this.requestOptions)) {
         results.addAll(CollectionUtil.map(response.getHits().getHits(), (searchHit) -> {
            return this.searchHitToObject(searchHit, clazz, objectMapper);
         }));
         scrollId = response.getScrollId();
         scrollRequest = (new SearchScrollRequest(scrollId)).scroll(TimeValue.timeValueMillis(60000L));
      }

      if (scrollId != null) {
         ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
         clearScrollRequest.addScrollId(scrollId);
         this.esClient.clearScroll(clearScrollRequest, this.requestOptions);
      }

      return results;
   }

   private Object searchHitToObject(SearchHit searchHit, Class clazz, ObjectMapper objectMapper) {
      try {
         return objectMapper.readValue(searchHit.getSourceAsString(), clazz);
      } catch (JsonProcessingException var5) {
         throw new OperateRuntimeException(String.format("Error while reading entity of type %s from Elasticsearch!", clazz.getName()), var5);
      }
   }

   private Object executeWithRetries(RetryOperation.RetryConsumer retryConsumer) {
      return this.executeWithRetries("", retryConsumer, (RetryOperation.RetryPredicate)null);
   }

   private Object executeWithRetries(String operationName, RetryOperation.RetryConsumer retryConsumer) {
      return this.executeWithRetries(operationName, retryConsumer, (RetryOperation.RetryPredicate)null);
   }

   private Object executeWithRetries(String operationName, RetryOperation.RetryConsumer retryConsumer, RetryOperation.RetryPredicate retryPredicate) {
      return this.executeWithGivenRetries(this.numberOfRetries, operationName, retryConsumer, retryPredicate);
   }

   private Object executeWithGivenRetries(int retries, String operationName, RetryOperation.RetryConsumer retryConsumer, RetryOperation.RetryPredicate retryPredicate) {
      try {
         return RetryOperation.newBuilder().retryConsumer(retryConsumer).retryPredicate(retryPredicate).noOfRetry(retries).delayInterval(this.delayIntervalInSeconds, TimeUnit.SECONDS).retryOn(new Class[]{IOException.class, ElasticsearchException.class}).retryPredicate(retryPredicate).message(operationName).build().retry();
      } catch (Exception var6) {
         throw new OperateRuntimeException("Couldn't execute operation " + operationName + " on elasticsearch for " + this.numberOfRetries + " attempts with " + this.delayIntervalInSeconds + " seconds waiting.", var6);
      }
   }
}
