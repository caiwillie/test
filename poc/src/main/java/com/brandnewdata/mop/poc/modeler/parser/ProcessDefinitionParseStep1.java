package com.brandnewdata.mop.poc.modeler.parser;

import com.brandnewdata.connector.api.IConnectorConfFeign;

public interface ProcessDefinitionParseStep1 extends FinalStep {
    ProcessDefinitionParseStep2 replaceStep1();

    ProcessDefinitionParseStep2 replaceProperties(IConnectorConfFeign client);
}
