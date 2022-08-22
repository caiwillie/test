package com.brandnewdata.mop.poc.operate.entity.listview;

import com.brandnewdata.mop.poc.operate.entity.OperateZeebeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.operate.dto.ProcessInstanceState;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class ProcessInstanceForListViewEntity extends OperateZeebeEntity<ProcessInstanceForListViewEntity> {
    private Long processDefinitionKey;
    private String processName;
    private Integer processVersion;
    private String bpmnProcessId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private ProcessInstanceState state;
    private List<String> batchOperationIds;
    private Long parentProcessInstanceKey;
    private Long parentFlowNodeInstanceKey;
    private String treePath;
    private boolean incident;
    private ListViewJoinRelation joinRelation;
    @JsonIgnore
    private Object[] sortValues;

}
