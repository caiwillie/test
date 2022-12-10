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
        String str = "{\"msg\":\"\",\"body\":\"{\\\"description\\\":\\\"cdshi001\\\",\\\"alertId\\\":\\\"5SibV2z62FwX\\\",\\\"theme\\\":\\\"simple\\\",\\\"nodeIP\\\":\\\"10.10.10.10\\\",\\\"camearaId\\\":29,\\\"policy\\\":\\\"{\\\\r       \\\\\\\"interval\\\\\\\": 10,\\\\r       \\\\\\\"filters\\\\\\\": [\\\\r           {\\\\r               \\\\\\\"field\\\\\\\": \\\\\\\"person\\\\\\\",\\\\r               \\\\\\\"threshold\\\\\\\": 0.1\\\\r           }\\\\r       ],\\\\r       \\\\\\\"type\\\\\\\": \\\\\\\"simple\\\\\\\"\\\\r   }\\\",\\\"datas\\\":[\\\"29-5SibV2z62FwX-1668052544.5260527-0.jpg\\\",\\\"29-5SibV2z62FwX-1668052544.5278604-1.jpg\\\",\\\"29-5SibV2z62FwX-1668052546.5325038-2.jpg\\\",\\\"29-5SibV2z62FwX-1668052546.534225-3.jpg\\\",\\\"29-5SibV2z62FwX-1668052548.53831-4.jpg\\\"]}\",\"data\":true,\"params\":{\"path\":{},\"query\":{}},\"status\":\"0\",\"headers\":{\"content-length\":\"638\",\"x-forwarded-proto\":\"http\",\"x-forwarded-host\":\"10.100.25.62:9997\",\"postman-token\":\"a9e49631-c6a6-472f-8bd0-a354cfb9cbf7\",\"host\":\"10.244.142.4:9060\",\"g2-domain\":\"http-listener.3gc4y21p2.g2.gongshu.gov.cn\",\"x-forwarded-port\":\"9997\",\"content-type\":\"application/json\",\"accept-encoding\":\"gzip, deflate, br\",\"forwarded\":\"proto=http;host=\\\"10.100.25.62:9997\\\";for=\\\"10.244.166.128:54414\\\"\",\"user-agent\":\"PostmanRuntime/7.28.4\",\"accept\":\"*/*\"},\"baseStatus\":\"OK\"}";
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
        String xml = ResourceUtil.readUtf8Str("process/process5.xml");
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.step1(null, null, xml);
        step1.replServiceTask(false, null);
        Step1Result step1Result = step1.step1Result();
        return;
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme