package com.brandnewdata.mop.poc.modeler.parser;

import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;

public interface ProcessDefinitionParseStep2 {

    ProcessDefinition buildProcessDefinition();
    ProcessDefinitionParseStep3 replaceTriggerStartEvent();

    ProcessDefinitionParseStep3 replaceConnectorStartEvent();

    ProcessDefinitionParseStep3 replaceCustomStartEvent();
}
