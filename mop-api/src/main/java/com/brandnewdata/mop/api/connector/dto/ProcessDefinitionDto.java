package com.brandnewdata.mop.api.connector.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessDefinitionDto {
    private String processId;
    private String processName;
    private String processXml;
}
