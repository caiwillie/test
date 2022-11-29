package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionStaticParseDto;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import org.springframework.stereotype.Service;

@Service
public class ProcessDefinitionService2 implements IProcessDefinitionService2{

    @Override
    public ProcessDefinitionStaticParseDto staticParse(BizDeployDto bizDeployDto) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(bizDeployDto.getProcessId(),
                bizDeployDto.getProcessName(), bizDeployDto.getProcessXml());

        Step1Result step1Result = step1.replServiceTask(true, null).replAttr().step1Result();
        ProcessDefinitionStaticParseDto ret = new ProcessDefinitionStaticParseDto();
        ret.setProcessId(step1Result.getProcessId());
        ret.setName(step1Result.getName());
        ret.setConfigs(step1Result.getConfigs());
        return ret;
    }
}
