package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.dto.parser.TriggerProcessDefinitionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;


class ProcessDefinitionDtoParserTest {


    @Mock
    private ConnectorManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNewInstance() {
        String xml = ResourceUtil.readUtf8Str("test.bpmn.xml");
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(null, null, xml);
        step1.step1Result();
        // Assertions.assertEquals(null, result);
    }

    @Test
    void testStep1() {
        String xml = ResourceUtil.readUtf8Str("test.bpmn.xml");
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(null, null, xml);
        ProcessDefinitionParseStep2 step2 = step1.replServiceTask(false, null).step2();
        step2.step2Result();
    }

    @Test
    void testTriggerXMLParse() {
        // 设置测试桩
        when(manager.getProtocol(any())).thenReturn("HTTP");

        String json = ResourceUtil.readUtf8Str("trigger.json");
        String triggerFullId = "com.develop:wjx.callback:v1";
        String xml = JSONUtil.parseObj(json).getStr("processEditing");
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(triggerFullId, null, xml);
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

        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(null, null, xml);

    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme