package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionStaticParseDto;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import org.springframework.stereotype.Service;

@Service
public class ProcessDefinitionService2 implements IProcessDefinitionService2{

    @Override
    public ProcessDefinitionStaticParseDto staticParse(BpmnXmlDto bpmnXmlDto) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(bpmnXmlDto.getProcessId(),
                bpmnXmlDto.getProcessName(), bpmnXmlDto.getProcessXml());

        Step1Result step1Result = step1.replServiceTask(true, null).replAttr().step1Result();
        ProcessDefinitionStaticParseDto ret = new ProcessDefinitionStaticParseDto();
        ret.setProcessId(step1Result.getProcessId());
        ret.setName(step1Result.getProcessName());
        ret.setConfigs(step1Result.getConnectorConfigMap());
        return ret;
    }

    @Override
    public BpmnXmlDto replaceProcessId(BpmnXmlDto dto) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(dto.getProcessId(),
                dto.getProcessName(), dto.getProcessXml());
        String newProcessId = StrUtil.format("Process_{}", IdUtil.simpleUUID());
        step1.replProcessId(newProcessId);
        Step1Result step1Result = step1.step1Result();
        BpmnXmlDto ret = new BpmnXmlDto();
        ret.setProcessId(step1Result.getProcessId());
        ret.setProcessName(step1Result.getProcessName());
        ret.setProcessXml(step1Result.getOriginalXml());
        return ret;
    }
}
