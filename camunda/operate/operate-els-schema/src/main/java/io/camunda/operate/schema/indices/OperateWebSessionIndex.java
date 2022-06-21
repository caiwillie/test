package io.camunda.operate.schema.indices;

import org.springframework.stereotype.Component;

@Component
public class OperateWebSessionIndex extends AbstractIndexDescriptor {
   public static final String ID = "id";
   public static final String CREATION_TIME = "creationTime";
   public static final String LAST_ACCESSED_TIME = "lastAccessedTime";
   public static final String MAX_INACTIVE_INTERVAL_IN_SECONDS = "maxInactiveIntervalInSeconds";
   public static final String ATTRIBUTES = "attributes";
   public static final String INDEX_NAME = "web-session";

   public String getIndexName() {
      return "web-session";
   }

   public String getVersion() {
      return "1.1.0";
   }
}
