package io.camunda.operate.schema.indices;

import org.springframework.stereotype.Component;

@Component
public class MetricIndex extends AbstractIndexDescriptor {
   public static final String INDEX_NAME = "metric";
   public static final String ID = "id";
   public static final String EVENT = "event";
   public static final String VALUE = "value";
   public static final String EVENT_TIME = "eventTime";

   public String getIndexName() {
      return "metric";
   }
}
