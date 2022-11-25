package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionStaticParseDto;

public interface IProcessDefinitionService2 {
    ProcessDefinitionStaticParseDto staticParse(BizDeployDto dto);
}
