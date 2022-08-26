package com.brandnewdata.mop.poc.operate.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ListViewProcessInstanceDTO extends OperateZeebeDTO {
    private String id;
    private String processId;
    private String processName;
    private Integer processVersion;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ProcessInstanceStateDto state;
    private String bpmnProcessId;
    private boolean hasActiveOperation = false;
    private List<OperationDTO> operations = new ArrayList<OperationDTO>();
    private String parentInstanceId;
    private String rootInstanceId;
    private List<ProcessInstanceReferenceDTO> callHierarchy = new ArrayList<ProcessInstanceReferenceDTO>();
    private String[] sortValues;
}
