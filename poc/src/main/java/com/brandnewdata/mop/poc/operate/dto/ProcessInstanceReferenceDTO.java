package com.brandnewdata.mop.poc.operate.dto;

import lombok.Data;

@Data
public class ProcessInstanceReferenceDTO {

    private String instanceId;

    private String processDefinitionId;

    private String processDefinitionName;

}
