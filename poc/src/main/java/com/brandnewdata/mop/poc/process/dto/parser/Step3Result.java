package com.brandnewdata.mop.poc.process.dto.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Step3Result {

    private String processId;

    private String name;

    private String xml;

    private TriggerOrOperate trigger;

    private String protocol;

    private ObjectNode requestParams;

    private ObjectNode responseParams;
}
