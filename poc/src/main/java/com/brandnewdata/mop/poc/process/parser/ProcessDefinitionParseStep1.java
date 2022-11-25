package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;

import java.util.Map;

public interface ProcessDefinitionParseStep1 {

    ProcessDefinitionParseStep1 parseConfig();
    ProcessDefinitionParseStep1 replProcessId(String processId);
    ProcessDefinitionParseStep1 replConfigId(Map<String, String> configMapping);

    ProcessDefinitionParseStep1 replServiceTask(boolean replConfig, ConnectorManager manager);

    ProcessDefinitionParseStep1 replAttr();

    ProcessDefinitionParseStep2 step2();

    Step1Result step1Result();
}
