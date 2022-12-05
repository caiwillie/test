package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionStaticParseDto;

public interface IProcessDefinitionService2 {
    ProcessDefinitionStaticParseDto staticParse(BpmnXmlDto dto);

    BpmnXmlDto replaceProcessId(BpmnXmlDto dto);
}
