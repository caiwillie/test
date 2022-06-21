package io.camunda.operate.schema.templates;

public interface ProcessInstanceDependant {
   String PROCESS_INSTANCE_KEY = "processInstanceKey";

   String getFullQualifiedName();
}
