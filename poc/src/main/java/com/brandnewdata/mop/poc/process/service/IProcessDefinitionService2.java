package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionParseDto;

import java.util.Map;

public interface IProcessDefinitionService2 {
    BpmnXmlDto baseCheck(BpmnXmlDto dto);

    Map<String, String> parseConfigMap(BpmnXmlDto dto);

    ProcessDefinitionParseDto parseSceneTrigger(BpmnXmlDto dto);
}
