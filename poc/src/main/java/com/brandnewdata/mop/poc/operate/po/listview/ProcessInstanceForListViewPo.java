package com.brandnewdata.mop.poc.operate.po.listview;

import com.brandnewdata.mop.poc.operate.po.OperateZeebePo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.operate.dto.ProcessInstanceState;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class ProcessInstanceForListViewPo extends OperateZeebePo<ProcessInstanceForListViewPo> {
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
