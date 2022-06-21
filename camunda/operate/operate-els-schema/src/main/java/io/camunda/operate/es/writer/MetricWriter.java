package io.camunda.operate.es.writer;

import io.camunda.operate.entities.MetricEntity;
import io.camunda.operate.es.contract.MetricContract;
import io.camunda.operate.es.dao.UsageMetricDAO;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricWriter implements MetricContract.Writer {
   private static final Logger LOGGER = LoggerFactory.getLogger(MetricWriter.class);
   @Autowired
   private UsageMetricDAO dao;

   public IndexRequest registerProcessInstanceCompleteEvent(String processInstanceKey, OffsetDateTime timestamp) {
      MetricEntity metric = this.createProcessInstanceFinishedKey(processInstanceKey, timestamp);
      return this.dao.buildESIndexRequest(metric);
   }

   public IndexRequest registerDecisionInstanceCompleteEvent(String decisionInstanceKey, OffsetDateTime timestamp) {
      MetricEntity metric = this.createDecisionsInstanceEvaluatedKey(decisionInstanceKey, timestamp);
      return this.dao.buildESIndexRequest(metric);
   }

   private MetricEntity createProcessInstanceFinishedKey(String processInstanceKey, OffsetDateTime timestamp) {
      return (MetricEntity)(new MetricEntity()).setEvent("EVENT_PROCESS_INSTANCE_FINISHED").setValue(processInstanceKey).setEventTime(timestamp).setId(UUID.randomUUID().toString());
   }

   private MetricEntity createDecisionsInstanceEvaluatedKey(String decisionInstanceKey, OffsetDateTime timestamp) {
      return (MetricEntity)(new MetricEntity()).setEvent("EVENT_DECISION_INSTANCE_EVALUATED").setValue(decisionInstanceKey).setEventTime(timestamp).setId(UUID.randomUUID().toString());
   }
}
