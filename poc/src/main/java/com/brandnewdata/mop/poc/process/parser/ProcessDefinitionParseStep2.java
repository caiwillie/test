package com.brandnewdata.mop.poc.process.parser;

import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.dto.parser.Step2Result;

public interface ProcessDefinitionParseStep2 {

    ProcessDefinitionParseStep2 replSE_trigger(ConnectorManager manager);

    ProcessDefinitionParseStep2 replSE_operate();

    ProcessDefinitionParseStep2 replSE_scene(ConnectorManager manager);

    ProcessDefinitionParseStep3 step3();

    Step2Result step2Result();
}
