package io.camunda.operate.schema.templates;

import io.camunda.operate.entities.IncidentState;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;

@Component
public class IncidentTemplate extends AbstractTemplateDescriptor implements ProcessInstanceDependant {
   public static QueryBuilder ACTIVE_INCIDENT_QUERY;
   public static final String INDEX_NAME = "incident";
   public static final String ID = "id";
   public static final String KEY = "key";
   public static final String PROCESS_INSTANCE_KEY = "processInstanceKey";
   public static final String PROCESS_KEY = "processDefinitionKey";
   public static final String FLOW_NODE_ID = "flowNodeId";
   public static final String FLOW_NODE_INSTANCE_KEY = "flowNodeInstanceKey";
   public static final String JOB_KEY = "jobKey";
   public static final String ERROR_TYPE = "errorType";
   public static final String ERROR_MSG = "errorMessage";
   public static final String ERROR_MSG_HASH = "errorMessageHash";
   public static final String STATE = "state";
   public static final String CREATION_TIME = "creationTime";
   public static final String TREE_PATH = "treePath";
   public static final String PENDING = "pending";

   public String getIndexName() {
      return "incident";
   }

   public String getVersion() {
      return "1.3.0";
   }

   static {
      ACTIVE_INCIDENT_QUERY = QueryBuilders.termQuery("state", IncidentState.ACTIVE);
   }
}
