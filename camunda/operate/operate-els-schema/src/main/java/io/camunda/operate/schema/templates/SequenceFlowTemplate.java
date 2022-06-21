package io.camunda.operate.schema.templates;

import org.springframework.stereotype.Component;

@Component
public class SequenceFlowTemplate extends AbstractTemplateDescriptor implements ProcessInstanceDependant {
   public static final String INDEX_NAME = "sequence-flow";
   public static final String ID = "id";
   public static final String KEY = "key";
   public static final String PROCESS_INSTANCE_KEY = "processInstanceKey";
   public static final String ACTIVITY_ID = "activityId";

   public String getIndexName() {
      return "sequence-flow";
   }
}
