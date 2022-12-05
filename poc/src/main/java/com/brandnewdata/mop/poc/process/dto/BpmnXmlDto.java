package com.brandnewdata.mop.poc.process.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class BpmnXmlDto {

    private String processId;

    private String processName;

    private String processXml;

}
