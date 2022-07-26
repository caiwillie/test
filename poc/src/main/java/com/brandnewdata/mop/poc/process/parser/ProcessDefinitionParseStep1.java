package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;

public interface ProcessDefinitionParseStep1 {
    ProcessDefinition buildProcessDefinition();

    ProcessDefinitionParseStep2 replaceStep1();

    ProcessDefinitionParseStep1 replaceProperties(ConnectorManager manager);
}
