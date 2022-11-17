package com.brandnewdata.mop.poc.operate.dto;

import lombok.Data;

@Data
public class ProcessInstanceReferenceDto {

    private String instanceId;

    private String processDefinitionId;

    private String processDefinitionName;

}
