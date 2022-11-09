package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.dto.parser.Step2Result;

public interface ProcessDefinitionParseStep2 {

    Step2Result step2Result();
    ProcessDefinitionParseStep3 replaceTriggerStartEvent(ConnectorManager manager);

    ProcessDefinitionParseStep3 replaceOperateStartEvent();

    ProcessDefinitionParseStep3 replaceSceneStartEvent(ConnectorManager manager);
}
