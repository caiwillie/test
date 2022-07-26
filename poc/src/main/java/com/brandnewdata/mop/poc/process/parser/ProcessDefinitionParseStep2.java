package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;

public interface ProcessDefinitionParseStep2 {

    ProcessDefinition buildProcessDefinition();
    ProcessDefinitionParseStep3 replaceTriggerStartEvent();

    ProcessDefinitionParseStep3 replaceConnectorStartEvent();

    ProcessDefinitionParseStep3 replaceSceneStartEvent(ConnectorManager manager);
}
