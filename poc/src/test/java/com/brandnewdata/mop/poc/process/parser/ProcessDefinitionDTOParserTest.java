package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionDTO;
import com.brandnewdata.mop.poc.process.dto.parser.TriggerProcessDefinitionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;


class ProcessDefinitionDTOParserTest {


    @Mock
    private ConnectorManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNewInstance() {
        String xml = ResourceUtil.readUtf8Str("test.bpmn.xml");
        ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
        processDefinitionDTO.setProcessId(null);
        processDefinitionDTO.setName(null);
        processDefinitionDTO.setXml(xml);
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.newInstance(processDefinitionDTO);
        step1.buildProcessDefinition();
        // Assertions.assertEquals(null, result);
    }

    @Test
    void testStep1() {
        String xml = ResourceUtil.readUtf8Str("test.bpmn.xml");
        ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
        processDefinitionDTO.setProcessId(null);
        processDefinitionDTO.setName(null);
        processDefinitionDTO.setXml(xml);
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.newInstance(processDefinitionDTO);
        ProcessDefinitionParseStep2 step2 = step1.replaceStep1();
        step2.buildProcessDefinition();
    }

    @Test
    void testTriggerXMLParse() {
        // 设置测试桩
        when(manager.getProtocol(any())).thenReturn("HTTP");

        String json = ResourceUtil.readUtf8Str("trigger.json");
        String triggerFullId = "com.develop:wjx.callback:v1";
        String xml = JSONUtil.parseObj(json).getStr("processEditing");
        ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
        processDefinitionDTO.setProcessId(triggerFullId);
        processDefinitionDTO.setXml(xml);
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.newInstance(processDefinitionDTO);
        TriggerProcessDefinitionDTO triggerProcessDefinition = step1.replaceStep1().replaceTriggerStartEvent(manager)
                .buildTriggerProcessDefinition();
        return;
    }

    @Test
    void testSceneCustomTriggerXMLParse() {
        String json = ResourceUtil.readUtf8Str("trigger.json");
        String xml2 = JSONUtil.parseObj(json).getStr("processEditing");

        // 设置测试桩
        when(manager.getTriggerXML(any())).thenReturn(xml2);
        when(manager.getProtocol(any())).thenReturn("HTTP");

        String xml = ResourceUtil.readUtf8Str("test.bpmn.xml");
        ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
        processDefinitionDTO.setXml(xml);
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.newInstance(processDefinitionDTO);
        TriggerProcessDefinitionDTO triggerProcessDefinition = step1.replaceStep1().replaceSceneStartEvent(manager)
                .buildTriggerProcessDefinition();
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme