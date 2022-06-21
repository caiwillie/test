package io.camunda.operate.schema.indices;

import org.springframework.stereotype.Component;

@Component
public class DecisionIndex extends AbstractIndexDescriptor {
   public static final String INDEX_NAME = "decision";
   public static final String ID = "id";
   public static final String KEY = "key";
   public static final String DECISION_ID = "decisionId";
   public static final String NAME = "name";
   public static final String VERSION = "version";
   public static final String DECISION_REQUIREMENTS_ID = "decisionRequirementsId";
   public static final String DECISION_REQUIREMENTS_KEY = "decisionRequirementsKey";

   public String getIndexName() {
      return "decision";
   }
}
