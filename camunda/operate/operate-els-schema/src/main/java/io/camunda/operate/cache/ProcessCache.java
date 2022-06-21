package io.camunda.operate.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.ProcessEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.indices.ProcessIndex;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ThreadUtil;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProcessCache {
   private static final Logger logger = LoggerFactory.getLogger(ProcessCache.class);
   private Map cache = new ConcurrentHashMap();
   private static final int CACHE_MAX_SIZE = 100;
   private static final int MAX_ATTEMPTS = 5;
   private static final long WAIT_TIME = 200L;
   @Autowired
   private RestHighLevelClient esClient;
   @Autowired
   private ProcessIndex processIndex;
   @Autowired
   private ObjectMapper objectMapper;

   public String getProcessNameOrDefaultValue(Long processDefinitionKey, String defaultValue) {
      ProcessEntity cachedProcessData = (ProcessEntity)this.cache.get(processDefinitionKey);
      String processName = defaultValue;
      if (cachedProcessData != null) {
         processName = cachedProcessData.getName();
      } else {
         Optional processMaybe = this.findOrWaitProcess(processDefinitionKey, 5, 200L);
         if (processMaybe.isPresent()) {
            ProcessEntity process = (ProcessEntity)processMaybe.get();
            this.putToCache(processDefinitionKey, process);
            processName = process.getName();
         }
      }

      if (StringUtils.isEmpty(processName)) {
         logger.debug("ProcessName is empty, use default value: {} ", defaultValue);
         processName = defaultValue;
      }

      return processName;
   }

   public String getProcessNameOrBpmnProcessId(Long processDefinitionKey, String defaultValue) {
      ProcessEntity cachedProcessData = (ProcessEntity)this.cache.get(processDefinitionKey);
      String processName = null;
      if (cachedProcessData == null) {
         Optional processMaybe = this.findOrWaitProcess(processDefinitionKey, 5, 200L);
         if (processMaybe.isPresent()) {
            cachedProcessData = (ProcessEntity)processMaybe.get();
            this.putToCache(processDefinitionKey, cachedProcessData);
         }
      }

      if (cachedProcessData != null) {
         processName = cachedProcessData.getName();
         if (processName == null) {
            processName = cachedProcessData.getBpmnProcessId();
         }
      }

      if (StringUtils.isEmpty(processName)) {
         logger.debug("ProcessName is empty, use default value: {} ", defaultValue);
         processName = defaultValue;
      }

      return processName;
   }

   private Optional readProcessByKey(Long processDefinitionKey) {
      try {
         return Optional.of(this.getProcess(processDefinitionKey));
      } catch (OperateRuntimeException var3) {
         return Optional.empty();
      }
   }

   public Optional findOrWaitProcess(Long processDefinitionKey, int attempts, long sleepInMilliseconds) {
      int attemptsCount = 0;
      Optional foundProcess = Optional.empty();

      while(!foundProcess.isPresent() && attemptsCount < attempts) {
         ++attemptsCount;
         foundProcess = this.readProcessByKey(processDefinitionKey);
         if (!foundProcess.isPresent()) {
            logger.debug("Unable to find process {}. {} attempts left. Waiting {} ms.", new Object[]{processDefinitionKey, attempts - attemptsCount, sleepInMilliseconds});
            ThreadUtil.sleepFor(sleepInMilliseconds);
         } else {
            logger.debug("Found process {} after {} attempts. Waited {} ms.", new Object[]{processDefinitionKey, attemptsCount, (long)(attemptsCount - 1) * sleepInMilliseconds});
         }
      }

      return foundProcess;
   }

   public void putToCache(Long processDefinitionKey, ProcessEntity process) {
      if (this.cache.size() >= 100) {
         Iterator iterator = this.cache.keySet().iterator();
         if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
         }
      }

      this.cache.put(processDefinitionKey, process);
   }

   public void clearCache() {
      this.cache.clear();
   }

   public ProcessEntity getProcess(Long processDefinitionKey) {
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.processIndex.getAlias()})).source((new SearchSourceBuilder()).query(QueryBuilders.termQuery("key", processDefinitionKey)));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value == 1L) {
            return this.fromSearchHit(response.getHits().getHits()[0].getSourceAsString());
         } else if (response.getHits().getTotalHits().value > 1L) {
            throw new OperateRuntimeException(String.format("Could not find unique process with key '%s'.", processDefinitionKey));
         } else {
            throw new OperateRuntimeException(String.format("Could not find process with key '%s'.", processDefinitionKey));
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
}
