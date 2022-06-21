package io.camunda.operate.schema.templates;

import org.springframework.stereotype.Component;

@Component
public class VariableTemplate extends AbstractTemplateDescriptor implements ProcessInstanceDependant {
   public static final String INDEX_NAME = "variable";
   public static final String ID = "id";
   public static final String KEY = "key";
   public static final String SCOPE_KEY = "scopeKey";
   public static final String NAME = "name";
   public static final String VALUE = "value";
   public static final String FULL_VALUE = "fullValue";
   public static final String IS_PREVIEW = "isPreview";

   public String getIndexName() {
      return "variable";
   }

   public String getVersion() {
      return "1.1.0";
   }
}
