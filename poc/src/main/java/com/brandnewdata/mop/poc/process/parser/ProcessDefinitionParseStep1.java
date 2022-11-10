package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.dto.parser.Step1Result;

public interface ProcessDefinitionParseStep1 {

    ProcessDefinitionParseStep1 parseConfig();

    ProcessDefinitionParseStep1 replServiceTask(boolean replConfig, ConnectorManager manager);

    ProcessDefinitionParseStep1 replAttr();

    ProcessDefinitionParseStep2 step2();

    Step1Result step1Result();
}
