package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;

public interface ProcessDefinitionParseStep2 {

    ProcessDefinition buildProcessDefinition();
    ProcessDefinitionParseStep3 replaceTriggerStartEvent(ConnectorManager manager);

    ProcessDefinitionParseStep3 replaceOperateStartEvent();

    ProcessDefinitionParseStep3 replaceSceneStartEvent(ConnectorManager manager);
}
