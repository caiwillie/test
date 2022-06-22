/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.listview.ProcessInstanceForListViewEntity
 *  io.camunda.operate.entities.listview.ProcessInstanceState
 *  io.camunda.operate.schema.templates.ListViewTemplate
 *  io.camunda.operate.schema.templates.OperationTemplate
 *  io.camunda.operate.schema.templates.ProcessInstanceDependant
 *  io.camunda.operate.webapp.es.reader.ProcessInstanceReader
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.client.RestHighLevelClient
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.index.reindex.BulkByScrollResponse
 *  org.elasticsearch.index.reindex.DeleteByQueryRequest
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
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
import org.elasticsearch.index.query.QueryBuilder;
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
    public static final List<ProcessInstanceState> STATES_FOR_DELETION = List.of(ProcessInstanceState.COMPLETED, ProcessInstanceState.CANCELED);
    @Autowired
    private RestHighLevelClient esClient;
    @Autowired
    private ListViewTemplate processInstanceTemplate;
    @Autowired
    private List<ProcessInstanceDependant> processInstanceDependantTemplates;
    @Autowired
    private ProcessInstanceReader processInstanceReader;

    public void deleteInstanceById(Long id) throws IOException {
        ProcessInstanceForListViewEntity processInstanceEntity = this.processInstanceReader.getProcessInstanceByKey(id);
        ProcessInstanceWriter.validateDeletion(processInstanceEntity);
        this.deleteProcessInstanceAndDependants(processInstanceEntity.getProcessInstanceKey().toString());
    }

    public static void validateDeletion(ProcessInstanceForListViewEntity processInstanceEntity) {
        if (!STATES_FOR_DELETION.contains(processInstanceEntity.getState())) {
            throw new IllegalArgumentException(String.format("Process instances needs to be in one of the states %s", STATES_FOR_DELETION));
        }
        if (processInstanceEntity.getEndDate() == null) throw new IllegalArgumentException(String.format("Process instances needs to have an endDate before now: %s < %s", processInstanceEntity.getEndDate(), OffsetDateTime.now()));
        if (!processInstanceEntity.getEndDate().isAfter(OffsetDateTime.now())) return;
        throw new IllegalArgumentException(String.format("Process instances needs to have an endDate before now: %s < %s", processInstanceEntity.getEndDate(), OffsetDateTime.now()));
    }

    private void deleteProcessInstanceAndDependants(String processInstanceKey) throws IOException {
        List processInstanceDependantsWithoutOperation = this.processInstanceDependantTemplates.stream().filter(t -> !(t instanceof OperationTemplate)).collect(Collectors.toList());
        Iterator iterator = processInstanceDependantsWithoutOperation.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                this.deleteDocument(this.processInstanceTemplate.getIndexPattern(), "processInstanceKey", processInstanceKey);
                return;
            }
            ProcessInstanceDependant template = (ProcessInstanceDependant)iterator.next();
            this.deleteDocument(template.getFullQualifiedName() + "*", "processInstanceKey", processInstanceKey);
        }
    }

    private long deleteDocument(String indexName, String idField, String id) throws IOException {
        DeleteByQueryRequest query = new DeleteByQueryRequest(new String[]{indexName}).setQuery((QueryBuilder)QueryBuilders.termsQuery((String)idField, (String[])new String[]{id}));
        BulkByScrollResponse response = this.esClient.deleteByQuery(query, RequestOptions.DEFAULT);
        logger.debug("Delete document {} in {} response: {}", id, indexName, response.getStatus());
        return response.getDeleted();
    }
}
