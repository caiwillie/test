package com.brandnewdata.mop.poc.operate.schema.template;

import org.springframework.stereotype.Component;

@Component
public class EventTemplate extends AbstractTemplateDescriptor implements ProcessInstanceDependant{
    public static final String INDEX_NAME = "event";
    public static final String ID = "id";
    public static final String KEY = "key";
    public static final String PROCESS_KEY = "processDefinitionKey";
    public static final String BPMN_PROCESS_ID = "bpmnProcessId";
    public static final String FLOW_NODE_ID = "flowNodeId";
    public static final String FLOW_NODE_INSTANCE_KEY = "flowNodeInstanceKey";
    public static final String EVENT_SOURCE_TYPE = "eventSourceType";
    public static final String EVENT_TYPE = "eventType";
    public static final String DATE_TIME = "dateTime";
    public static final String METADATA = "metadata";
    public static final String JOB_TYPE = "jobType";
    public static final String JOB_RETRIES = "jobRetries";
    public static final String JOB_WORKER = "jobWorker";
    public static final String JOB_DEADLINE = "jobDeadline";
    public static final String JOB_CUSTOM_HEADERS = "jobCustomHeaders";
    public static final String INCIDENT_ERROR_TYPE = "incidentErrorType";
    public static final String INCIDENT_ERROR_MSG = "incidentErrorMessage";
    public static final String JOB_KEY = "jobKey";

    @Override
    public String getIndexName() {
        return "event";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
