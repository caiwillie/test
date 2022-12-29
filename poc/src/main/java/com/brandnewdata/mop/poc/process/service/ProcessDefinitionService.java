package com.brandnewdata.mop.poc.process.service;

import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionParseDto;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParseStep1;
import com.brandnewdata.mop.poc.process.parser.ProcessDefinitionParser;
import com.brandnewdata.mop.poc.process.parser.dto.Action;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import com.brandnewdata.mop.poc.process.parser.dto.Step2Result;
import com.dxy.library.json.jackson.JacksonUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProcessDefinitionService implements IProcessDefinitionService {

    private final ConnectorManager connectorManager;

    public ProcessDefinitionService(ConnectorManager connectorManager) {
        this.connectorManager = connectorManager;
    }

    @Override
    public BpmnXmlDto baseCheck(BpmnXmlDto dto) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(dto.getProcessId(),
                dto.getProcessName(), dto.getProcessXml());

        Step1Result step1Result = step1.replServiceTask(false, null).replAttr().step1Result();
        BpmnXmlDto ret = new BpmnXmlDto(step1Result.getProcessId(), step1Result.getProcessName(), step1Result.getOriginalXml());
        return ret;
    }

    @Override
    public Map<String, String> parseConfigMap(BpmnXmlDto dto) {
        // 解析流程中用到的流程
        Step1Result step1Result = ProcessDefinitionParser.step1(dto .getProcessId(), dto.getProcessName(), dto.getProcessXml())
                .parseConfig().step1Result();

        return step1Result.getConnectorConfigMap();
    }

    @Override
    public ProcessDefinitionParseDto parseSceneTrigger(BpmnXmlDto dto) {
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(dto.getProcessId(),
                dto.getProcessName(), dto.getProcessXml());
        Step2Result step2Result = step1.step2().replEleSceneSe(connectorManager).step2Result();
        ProcessDefinitionParseDto ret = new ProcessDefinitionParseDto();
        ret.setTriggerFullId(Opt.ofNullable(step2Result.getTrigger()).map(Action::getFullId).orElse(null));
        ret.setProtocol(step2Result.getProtocol());
        ret.setRequestParams(JacksonUtil.to(step2Result.getRequestParams()));
        return ret;
    }
}
