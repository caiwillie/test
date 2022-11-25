package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.parser.dto.Step2Result;

public interface ProcessDefinitionParseStep2 {

    ProcessDefinitionParseStep2 replEleTriggerSe(ConnectorManager manager);

    ProcessDefinitionParseStep2 replEleOperateSe();

    ProcessDefinitionParseStep2 replEleSceneSe(ConnectorManager manager);

    Step2Result step2Result();
}
