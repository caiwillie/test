package io.camunda.operate.entities.dmn;

import io.camunda.operate.entities.FlowNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DecisionType {
   DECISION_TABLE,
   UNSPECIFIED,
   UNKNOWN;

   private static final Logger logger = LoggerFactory.getLogger(FlowNodeType.class);

   public static DecisionType fromZeebeDecisionType(String decisionType) {
      if (decisionType == null) {
         return UNSPECIFIED;
      } else {
         try {
            return valueOf(decisionType);
         } catch (IllegalArgumentException var2) {
            logger.error("Decision type not found for value [{}]. UNKNOWN type will be assigned.", decisionType);
            return UNKNOWN;
         }
      }
   }
}
