package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionParseDto;

public interface IProcessDefinitionService2 {
    ProcessDefinitionParseDto parseIdAndName(BpmnXmlDto dto);

    ProcessDefinitionParseDto parseSceneTrigger(BpmnXmlDto dto);

    BpmnXmlDto replaceProcessId(BpmnXmlDto dto);
}
