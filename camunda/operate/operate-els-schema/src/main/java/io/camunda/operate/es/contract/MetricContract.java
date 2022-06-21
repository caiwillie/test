package io.camunda.operate.es.contract;

import java.time.OffsetDateTime;
import org.elasticsearch.action.index.IndexRequest;

public interface MetricContract {
   String EVENT_PROCESS_INSTANCE_FINISHED = "EVENT_PROCESS_INSTANCE_FINISHED";
   String EVENT_DECISION_INSTANCE_EVALUATED = "EVENT_DECISION_INSTANCE_EVALUATED";

   public interface Writer {
      IndexRequest registerProcessInstanceCompleteEvent(String var1, OffsetDateTime var2);

      IndexRequest registerDecisionInstanceCompleteEvent(String var1, OffsetDateTime var2);
   }

   public interface Reader {
      Long retrieveProcessInstanceCount(OffsetDateTime var1, OffsetDateTime var2);

      Long retrieveDecisionInstanceCount(OffsetDateTime var1, OffsetDateTime var2);
   }
}
