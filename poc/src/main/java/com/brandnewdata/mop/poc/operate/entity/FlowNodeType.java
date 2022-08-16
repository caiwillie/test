package com.brandnewdata.mop.poc.operate.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum FlowNodeType {
    UNSPECIFIED,
    PROCESS,
    SUB_PROCESS,
    EVENT_SUB_PROCESS,
    START_EVENT,
    INTERMEDIATE_CATCH_EVENT,
    INTERMEDIATE_THROW_EVENT,
    BOUNDARY_EVENT,
    END_EVENT,
    SERVICE_TASK,
    RECEIVE_TASK,
    USER_TASK,
    MANUAL_TASK,
    EXCLUSIVE_GATEWAY,
    PARALLEL_GATEWAY,
    EVENT_BASED_GATEWAY,
    SEQUENCE_FLOW,
    MULTI_INSTANCE_BODY,
    CALL_ACTIVITY,
    BUSINESS_RULE_TASK,
    SCRIPT_TASK,
    SEND_TASK,
    UNKNOWN;

    private static final Logger logger = LoggerFactory.getLogger(FlowNodeType.class);

    private FlowNodeType() {
    }

    public static FlowNodeType fromZeebeBpmnElementType(String bpmnElementType) {
        if (bpmnElementType == null) {
            return UNSPECIFIED;
        } else {
            try {
                return valueOf(bpmnElementType);
            } catch (IllegalArgumentException var2) {
                logger.error("Flow node type not found for value [{}]. UNKNOWN type will be assigned.", bpmnElementType);
                return UNKNOWN;
            }
        }
    }
}
