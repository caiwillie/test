package com.brandnewdata.mop.poc.modeler.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

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
        step1.build();
        // Assertions.assertEquals(null, result);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme