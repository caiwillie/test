package com.brandnewdata.mop.poc.operate.po;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class EventPo extends OperateZeebePo<EventPo> {
    private Long processDefinitionKey;
    private Long processInstanceKey;
    private String bpmnProcessId;
    private String flowNodeId;
    private Long flowNodeInstanceKey;
    private EventSourceType eventSourceType;
    private EventType eventType;
    private OffsetDateTime dateTime;
    private EventMetadataPo metadata;

}
