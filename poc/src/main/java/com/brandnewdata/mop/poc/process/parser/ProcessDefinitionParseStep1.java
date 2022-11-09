package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDto;

public interface ProcessDefinitionParseStep1 {
    ProcessDefinitionDto buildProcessDefinition();

    ProcessDefinitionParseStep2 replaceStep1();

    ProcessDefinitionParseStep1 replaceProperties(ConnectorManager manager);
}
