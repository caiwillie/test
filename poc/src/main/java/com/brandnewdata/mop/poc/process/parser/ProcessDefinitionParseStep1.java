package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.dto.parser.Step1Result;

public interface ProcessDefinitionParseStep1 {
    Step1Result step1Result();

    ProcessDefinitionParseStep2 replaceStep1();

    ProcessDefinitionParseStep1 parseConnConfig(ConnectorManager manager);
}
