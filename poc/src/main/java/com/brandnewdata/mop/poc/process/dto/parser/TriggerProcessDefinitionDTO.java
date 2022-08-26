package com.brandnewdata.mop.poc.process.dto.parser;

import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDTO;
import com.brandnewdata.mop.poc.process.dto.parser.TriggerOrOperate;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class TriggerProcessDefinitionDTO extends ProcessDefinitionDTO {

    private TriggerOrOperate trigger;

    private String protocol;

    private ObjectNode requestParams;

    private ObjectNode responseParams;
}
