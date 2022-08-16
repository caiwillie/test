package com.brandnewdata.mop.poc.operate.entity;

import java.time.OffsetDateTime;

public class EventEntity extends OperateZeebeEntity<EventEntity> {
    private Long processDefinitionKey;
    private Long processInstanceKey;
    private String bpmnProcessId;
    private String flowNodeId;
    private Long flowNodeInstanceKey;
    private EventSourceType eventSourceType;
    private EventType eventType;
    private OffsetDateTime dateTime;
    private EventMetadataEntity metadata;

}
