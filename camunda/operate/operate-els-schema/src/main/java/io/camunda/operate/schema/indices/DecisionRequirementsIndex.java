package io.camunda.operate.schema.indices;

import org.springframework.stereotype.Component;

@Component
public class DecisionRequirementsIndex extends AbstractIndexDescriptor {
   public static final String INDEX_NAME = "decision-requirements";
   public static final String ID = "id";
   public static final String KEY = "key";
   public static final String DECISION_DEFINITION_ID = "decisionDefinitionId";
   public static final String NAME = "name";
   public static final String VERSION = "version";
   public static final String RESOURCE_NAME = "resourceName";
   public static final String XML = "xml";

   public String getIndexName() {
      return "decision-requirements";
   }
}
