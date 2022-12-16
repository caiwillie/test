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
        String str = "<?xml version=1.0 encoding=UTF-8?>\n" +
                "<bpmn2:definitions xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance xmlns:bpmn2=http://www.omg.org/spec/BPMN/20100524/MODEL xmlns:bpmndi=http://www.omg.org/spec/BPMN/20100524/DI xmlns:dc=http://www.omg.org/spec/DD/20100524/DC xmlns:di=http://www.omg.org/spec/DD/20100524/DI xmlns:brandnewdata=https://www.brandnewdata.com/schema/1.0 xmlns:zeebe=http://camunda.org/schema/zeebe/1.0 id=sample-diagram targetNamespace=http://bpmn.io/schema/bpmn xsi:schemaLocation=http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd>\n" +
                "  <bpmn2:process id=Process_d59fc418-ce19-4ec0-aa61-4953702fea06 isExecutable=false>\n" +
                "    <bpmn2:startEvent id=StartEvent_1 name=HttpListener - http触发器 brandnewdata:modelerIcon=https://baida-private.oss-cn-hangzhou.aliyuncs.com/1657006590055http通用连接器.svg brandnewdata:modelerGroup=com.brandnewdata brandnewdata:modelerUid=http-listener brandnewdata:modelerId=1544223454033076225 brandnewdata:modelerType=3 brandnewdata:modelerName=HttpListener brandnewdata:modelerVersion=1.0.0>\n" +
                "      <bpmn2:extensionElements>\n" +
                "        <brandnewdata:taskDefinition type=com.brandnewdata:http-listener.httpListener:1.0.0 />\n" +
                "        <brandnewdata:requestMapping label=监听配置>\n" +
                "          <brandnewdata:request type=string name=listenerPath label=监听路径 value=&#34;/v1/alert/callback&#34; required=false />\n" +
                "        </brandnewdata:requestMapping>\n" +
                "        <brandnewdata:responseMapping label=响应配置>\n" +
                "          <brandnewdata:response type=string name=headers label=响应头 value=headers required=false />\n" +
                "          <brandnewdata:response type=string name=body label=响应体 value=body required=false />\n" +
                "        </brandnewdata:responseMapping>\n" +
                "      </bpmn2:extensionElements>\n" +
                "      <bpmn2:outgoing>Flow_14mjj9h</bpmn2:outgoing>\n" +
                "    </bpmn2:startEvent>\n" +
                "    <bpmn2:endEvent id=Event_0q6k3py name=结束事件>\n" +
                "      <bpmn2:extensionElements />\n" +
                "      <bpmn2:incoming>Flow_14642c0</bpmn2:incoming>\n" +
                "    </bpmn2:endEvent>\n" +
                "    <bpmn2:intermediateThrowEvent id=Event_09rvtco name=中间事件>\n" +
                "      <bpmn2:extensionElements>\n" +
                "        <zeebe:ioMapping>\n" +
                "          <zeebe:output source== &#34;OK&#34; target=baseStatus />\n" +
                "          <zeebe:output source== true target=data />\n" +
                "          <zeebe:output source== &#34;&#34; target=msg />\n" +
                "          <zeebe:output source== &#34;0&#34; target=status />\n" +
                "        </zeebe:ioMapping>\n" +
                "      </bpmn2:extensionElements>\n" +
                "      <bpmn2:incoming>Flow_14mjj9h</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>Flow_14642c0</bpmn2:outgoing>\n" +
                "    </bpmn2:intermediateThrowEvent>\n" +
                "    <bpmn2:sequenceFlow id=Flow_14mjj9h sourceRef=StartEvent_1 targetRef=Event_09rvtco />\n" +
                "    <bpmn2:sequenceFlow id=Flow_14642c0 sourceRef=Event_09rvtco targetRef=Event_0q6k3py />\n" +
                "  </bpmn2:process>\n" +
                "  <bpmndi:BPMNDiagram id=BPMNDiagram_1>\n" +
                "    <bpmndi:BPMNPlane id=BPMNPlane_1 bpmnElement=Process_d59fc418-ce19-4ec0-aa61-4953702fea06>\n" +
                "      <bpmndi:BPMNEdge id=Flow_14mjj9h_di bpmnElement=Flow_14mjj9h>\n" +
                "        <di:waypoint x=510 y=64 />\n" +
                "        <di:waypoint x=510 y=146 />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=Flow_14642c0_di bpmnElement=Flow_14642c0>\n" +
                "        <di:waypoint x=510 y=194 />\n" +
                "        <di:waypoint x=510 y=276 />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNShape id=Event_190ucs2_di bpmnElement=StartEvent_1>\n" +
                "        <dc:Bounds x=378 y=16 width=263 height=48 />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=Event_0q6k3py_di bpmnElement=Event_0q6k3py>\n" +
                "        <dc:Bounds x=378 y=276 width=263 height=48 />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=Event_09rvtco_di bpmnElement=Event_09rvtco>\n" +
                "        <dc:Bounds x=378 y=146 width=263 height=48 />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn2:definitions>";
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
        String xml = ResourceUtil.readUtf8Str("process/process4.xml");
        ProcessDefinitionParser.step1(null, null, xml).step2()
                .replEleSceneSe(null);

        return;
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme