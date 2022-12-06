package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionParseDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import com.brandnewdata.mop.poc.process.parser.dto.Step2Result;
import com.dxy.library.json.jackson.JacksonUtil;
import org.springframework.stereotype.Service;

@Service
public class ProcessDefinitionService2 implements IProcessDefinitionService2{

    private final ConnectorManager connectorManager;

    public ProcessDefinitionService2(ConnectorManager connectorManager) {
        this.connectorManager = connectorManager;
    }

    @Override
    public ProcessDefinitionParseDto parseIdAndName(BpmnXmlDto dto) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(dto.getProcessId(),
                dto.getProcessName(), dto.getProcessXml());

        Step1Result step1Result = step1.replServiceTask(true, null).replAttr().step1Result();
        ProcessDefinitionParseDto ret = new ProcessDefinitionParseDto();
        ret.setProcessId(step1Result.getProcessId());
        ret.setName(step1Result.getProcessName());
        return ret;
    }

    @Override
    public ProcessDefinitionParseDto parseSceneTrigger(BpmnXmlDto dto) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(dto.getProcessId(),
                dto.getProcessName(), dto.getProcessXml());
        Step2Result step2Result = step1.step2().replEleSceneSe(connectorManager).step2Result();
        ProcessDefinitionParseDto ret = new ProcessDefinitionParseDto();
        ret.setTriggerFullId(step2Result.getTrigger().getFullId());
        ret.setProtocol(step2Result.getProtocol());
        ret.setRequestParams(JacksonUtil.to(step2Result.getRequestParams()));
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
