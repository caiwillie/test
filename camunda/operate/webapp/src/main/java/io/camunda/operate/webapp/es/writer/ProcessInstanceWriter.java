package io.camunda.operate.webapp.es.writer;

import io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity;
import io.camunda.operate.entities.listview.ProcessInstanceState;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.schema.templates.OperationTemplate;
import io.camunda.operate.schema.templates.ProcessInstanceDependant;
import io.camunda.operate.webapp.es.reader.ProcessInstanceReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceWriter {
   private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceWriter.class);
   public static final List STATES_FOR_DELETION;
   @Autowired
   private RestHighLevelClient esClient;
   @Autowired
   private ListViewTemplate processInstanceTemplate;
   @Autowired
   private List processInstanceDependantTemplates;
   @Autowired
   private ProcessInstanceReader processInstanceReader;

   public void deleteInstanceById(Long id) throws IOException {
      ProcessInstanceForListViewEntity processInstanceEntity = this.processInstanceReader.getProcessInstanceByKey(id);
      validateDeletion(processInstanceEntity);
      this.deleteProcessInstanceAndDependants(processInstanceEntity.getProcessInstanceKey().toString());
   }

   public static void validateDeletion(ProcessInstanceForListViewEntity processInstanceEntity) {
      if (!STATES_FOR_DELETION.contains(processInstanceEntity.getState())) {
         throw new IllegalArgumentException(String.format("Process instances needs to be in one of the states %s", STATES_FOR_DELETION));
      } else if (processInstanceEntity.getEndDate() == null || processInstanceEntity.getEndDate().isAfter(OffsetDateTime.now())) {
         throw new IllegalArgumentException(String.format("Process instances needs to have an endDate before now: %s < %s", processInstanceEntity.getEndDate(), OffsetDateTime.now()));
      }
   }

   private void deleteProcessInstanceAndDependants(String processInstanceKey) throws IOException {
      List processInstanceDependantsWithoutOperation = (List)this.processInstanceDependantTemplates.stream().filter((t) -> {
         return !(t instanceof OperationTemplate);
      }).collect(Collectors.toList());
      Iterator var3 = processInstanceDependantsWithoutOperation.iterator();

      while(var3.hasNext()) {
         ProcessInstanceDependant template = (ProcessInstanceDependant)var3.next();
         this.deleteDocument(template.getFullQualifiedName() + "*", "processInstanceKey", processInstanceKey);
      }

      this.deleteDocument(this.processInstanceTemplate.getIndexPattern(), "processInstanceKey", processInstanceKey);
   }

   private long deleteDocument(String indexName, String idField, String id) throws IOException {
      DeleteByQueryRequest query = (new DeleteByQueryRequest(new String[]{indexName})).setQuery(QueryBuilders.termsQuery(idField, new String[]{id}));
      BulkByScrollResponse response = this.esClient.deleteByQuery(query, RequestOptions.DEFAULT);
      logger.debug("Delete document {} in {} response: {}", new Object[]{id, indexName, response.getStatus()});
      return response.getDeleted();
   }

   static {
      STATES_FOR_DELETION = List.of(ProcessInstanceState.COMPLETED, ProcessInstanceState.CANCELED);
   }
}
