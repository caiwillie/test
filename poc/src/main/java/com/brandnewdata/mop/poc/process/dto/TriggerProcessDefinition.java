package com.brandnewdata.mop.poc.process.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class TriggerProcessDefinition extends ProcessDefinition {

    private TriggerOrOperate trigger;

    private String protocol;

    private ObjectNode requestParams;
}
