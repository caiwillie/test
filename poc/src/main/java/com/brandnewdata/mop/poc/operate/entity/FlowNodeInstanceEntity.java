package com.brandnewdata.mop.poc.operate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class FlowNodeInstanceEntity extends OperateZeebeEntity<FlowNodeInstanceEntity> {

    private String flowNodeId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private FlowNodeState state;
    private FlowNodeType type;
    private Long incidentKey;
    private Long processInstanceKey;
    private String treePath;
    private int level;
    private Long position;
    private boolean incident;
    @JsonIgnore
    private Object[] sortValues;

}
