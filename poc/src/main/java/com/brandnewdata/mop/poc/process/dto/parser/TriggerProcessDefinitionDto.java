package com.brandnewdata.mop.poc.process.dto.parser;

import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class TriggerProcessDefinitionDto extends ProcessDefinitionDto {

    private TriggerOrOperate trigger;

    private String protocol;

    private ObjectNode requestParams;

    private ObjectNode responseParams;
}
