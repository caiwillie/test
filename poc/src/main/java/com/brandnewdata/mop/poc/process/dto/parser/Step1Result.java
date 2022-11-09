package com.brandnewdata.mop.poc.process.dto.parser;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Step1Result {
    private String processId;
    private String name;
    private String xml;
    private Map<String, String> configMap;
}
