package com.brandnewdata.mop.poc.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class XMLDTO {

    private String modelKey;

    private String name;

    private String zeebeXML;

    private String triggerFullId;

    private String protocol;

    private ObjectNode requestParamConfigs;
}