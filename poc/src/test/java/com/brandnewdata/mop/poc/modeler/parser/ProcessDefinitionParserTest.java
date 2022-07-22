package com.brandnewdata.mop.poc.modeler.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import io.camunda.zeebe.client.ZeebeClient;
import org.dom4j.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class ProcessDefinitionParserTest {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNewInstance() {
        String xml = ResourceUtil.readUtf8Str("test.bpmn.xml");
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProcessId(null);
        processDefinition.setName(null);
        processDefinition.setXml(xml);
        ProcessDefinitionParseStep1 step1 = ProcessDefinitionParser.newInstance(processDefinition);
        // Assertions.assertEquals(null, result);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme