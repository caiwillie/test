package io.camunda.operate.schema.migration;

import io.camunda.operate.es.RetryElasticsearchClient;
import io.camunda.operate.exceptions.MigrationException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public interface Plan {
   static ReindexPlan forReindex() {
      return new ReindexPlan();
   }

   default List getSteps() {
      return Collections.emptyList();
   }

   void executeOn(RetryElasticsearchClient var1) throws IOException, MigrationException;
}
