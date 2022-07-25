package com.brandnewdata.mop.poc.modeler.parser;

import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;

public interface ProcessDefinitionParseStep1 {
    ProcessDefinition buildProcessDefinition();

    ProcessDefinitionParseStep2 replaceStep1();

    ProcessDefinitionParseStep2 replaceProperties(ConnectorManager manager);
}
