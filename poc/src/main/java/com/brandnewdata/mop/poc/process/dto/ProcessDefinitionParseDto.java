package com.brandnewdata.mop.poc.process.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProcessDefinitionParseDto {
    private String processId;
    private String name;
    private Map<String, String> configs;
    private String triggerFullId;
    private String protocol;
    private String requestParams;
}
