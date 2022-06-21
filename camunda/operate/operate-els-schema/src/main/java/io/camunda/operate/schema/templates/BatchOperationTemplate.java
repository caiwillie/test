package io.camunda.operate.schema.templates;

import org.springframework.stereotype.Component;

@Component
public class BatchOperationTemplate extends AbstractTemplateDescriptor {
   public static final String INDEX_NAME = "batch-operation";
   public static final String ID = "id";
   public static final String TYPE = "type";
   public static final String NAME = "name";
   public static final String USERNAME = "username";
   public static final String START_DATE = "startDate";
   public static final String END_DATE = "endDate";
   public static final String INSTANCES_COUNT = "instancesCount";
   public static final String OPERATIONS_TOTAL_COUNT = "operationsTotalCount";
   public static final String OPERATIONS_FINISHED_COUNT = "operationsFinishedCount";

   public String getIndexName() {
      return "batch-operation";
   }
}
