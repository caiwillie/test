package io.camunda.operate.schema.templates;

import org.springframework.stereotype.Component;

@Component
public class OperationTemplate extends AbstractTemplateDescriptor implements ProcessInstanceDependant {
   public static final String INDEX_NAME = "operation";
   public static final String ID = "id";
   public static final String TYPE = "type";
   public static final String PROCESS_INSTANCE_KEY = "processInstanceKey";
   public static final String INCIDENT_KEY = "incidentKey";
   public static final String SCOPE_KEY = "scopeKey";
   public static final String VARIABLE_NAME = "variableName";
   public static final String VARIABLE_VALUE = "variableValue";
   public static final String STATE = "state";
   public static final String ERROR_MSG = "errorMessage";
   public static final String LOCK_EXPIRATION_TIME = "lockExpirationTime";
   public static final String LOCK_OWNER = "lockOwner";
   public static final String BATCH_OPERATION_ID = "batchOperationId";
   public static final String ZEEBE_COMMAND_KEY = "zeebeCommandKey";
   public static final String USERNAME = "username";

   public String getIndexName() {
      return "operation";
   }
}
