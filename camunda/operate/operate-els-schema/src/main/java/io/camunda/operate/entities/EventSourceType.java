package io.camunda.operate.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum EventSourceType {
   JOB,
   PROCESS_INSTANCE,
   INCIDENT,
   UNKNOWN,
   UNSPECIFIED;

   private static final Logger logger = LoggerFactory.getLogger(EventSourceType.class);

   public static EventSourceType fromZeebeValueType(String valueType) {
      if (valueType == null) {
         return UNSPECIFIED;
      } else {
         try {
            return valueOf(valueType);
         } catch (IllegalArgumentException var2) {
            logger.error("Value type not found for value [{}]. UNKNOWN type will be assigned.", valueType);
            return UNKNOWN;
         }
      }
   }
}
