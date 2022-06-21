package io.camunda.operate.schema.indices;

import org.springframework.stereotype.Component;

@Component
public class ProcessIndex extends AbstractIndexDescriptor {
   public static final String INDEX_NAME = "process";
   public static final String ID = "id";
   public static final String KEY = "key";
   public static final String BPMN_PROCESS_ID = "bpmnProcessId";
   public static final String NAME = "name";
   public static final String VERSION = "version";
   public static final String BPMN_XML = "bpmnXml";
   public static final String RESOURCE_NAME = "resourceName";

   public String getIndexName() {
      return "process";
   }
}
