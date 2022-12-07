package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.parser.dto.Step1Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;


class ProcessDefinitionParserTest {

    @Mock
    private ConnectorManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void echo() {
        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:zeebe=\"http://camunda.org/schema/zeebe/1.0\" id=\"sample-diagram\" targetNamespace=\"http://bpmn.io/schema/bpmn\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">\n <bpmn2:process id=\"Process_833fa6b5-b004-4579-843a-6d3af29813dd\" isExecutable=\"false\">\n <bpmn2:startEvent id=\"StartEvent_1\" name=\"开始事件\">\n <bpmn2:outgoing>Flow_13a7p5z</bpmn2:outgoing>\n </bpmn2:startEvent>\n <bpmn2:sequenceFlow id=\"Flow_13a7p5z\" sourceRef=\"StartEvent_1\" targetRef=\"Activity_0c6vxm8\" />\n <bpmn2:serviceTask id=\"Activity_0c6vxm8\" name=\"请求RPA Worker\">\n <bpmn2:extensionElements>\n <zeebe:taskDefinition type=\"com.brandnewdata:rpa.send:1.0.0\" />\n <zeebe:ioMapping>\n <zeebe:input source=\"= inputs\" target=\"inputs\" />\n </zeebe:ioMapping>\n </bpmn2:extensionElements>\n <bpmn2:incoming>Flow_13a7p5z</bpmn2:incoming>\n <bpmn2:outgoing>Flow_1qlih64</bpmn2:outgoing>\n </bpmn2:serviceTask>\n <bpmn2:endEvent id=\"Event_0gzb9g2\" name=\"结束事件\">\n <bpmn2:incoming>Flow_1qlih64</bpmn2:incoming>\n </bpmn2:endEvent>\n <bpmn2:sequenceFlow id=\"Flow_1qlih64\" sourceRef=\"Activity_0c6vxm8\" targetRef=\"Event_0gzb9g2\" />\n </bpmn2:process>\n <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_833fa6b5-b004-4579-843a-6d3af29813dd\">\n <bpmndi:BPMNEdge id=\"Flow_1qlih64_di\" bpmnElement=\"Flow_1qlih64\">\n <di:waypoint x=\"544\" y=\"234\" />\n <di:waypoint x=\"544\" y=\"286\" />\n </bpmndi:BPMNEdge>\n <bpmndi:BPMNEdge id=\"Flow_13a7p5z_di\" bpmnElement=\"Flow_13a7p5z\">\n <di:waypoint x=\"544\" y=\"134\" />\n <di:waypoint x=\"544\" y=\"186\" />\n </bpmndi:BPMNEdge>\n <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">\n <dc:Bounds x=\"412\" y=\"86\" width=\"263\" height=\"48\" />\n </bpmndi:BPMNShape>\n <bpmndi:BPMNShape id=\"Activity_1ipyk9y_di\" bpmnElement=\"Activity_0c6vxm8\">\n <dc:Bounds x=\"412\" y=\"186\" width=\"263\" height=\"48\" />\n <bpmndi:BPMNLabel />\n </bpmndi:BPMNShape>\n <bpmndi:BPMNShape id=\"Event_0gzb9g2_di\" bpmnElement=\"Event_0gzb9g2\">\n <dc:Bounds x=\"412\" y=\"286\" width=\"263\" height=\"48\" />\n </bpmndi:BPMNShape>\n </bpmndi:BPMNPlane>\n </bpmndi:BPMNDiagram>\n</bpmn2:definitions>\n";
        System.out.println(str);
    }

    private String RESOURCE = "test2.bpmn.xml";

    @Test
    void allTest() {
        ProcessDefinitionParseStep1 step1 = testNewInstance();
        testStep1(step1);
    }

    ProcessDefinitionParseStep1 testNewInstance() {
        String xml = ResourceUtil.readUtf8Str(RESOURCE);
        return ProcessDefinitionParser.step1(null, null, xml);
        // Assertions.assertEquals(null, result);
    }

    ProcessDefinitionParseStep2 testStep1(ProcessDefinitionParseStep1 step1) {
         return step1.replAttr().replServiceTask(false, null).step2();
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


    /**
     * 测试标准的bpmn流程
     */
    @Test
    void testParseOriginalXml() {
        String xml = ResourceUtil.readUtf8Str("process/empty_process.xml");
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(null, null, xml);
        Step1Result step1Result = step1.step1Result();
        return;
    }

    @Test
    void testParseOriginalXml2() {
        String xml = ResourceUtil.readUtf8Str("process/process3.xml");
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(null, null, xml);
        step1.replServiceTask(false, null);
        Step1Result step1Result = step1.step1Result();
        return;
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme