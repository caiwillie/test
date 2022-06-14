package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class XMLParserTest {



    @Test
    void parse() {
        String content = ResourceUtil.readStr("demo.bpmn.xml", StandardCharsets.UTF_8);
        XMLDTO parse = new XMLParser().parse(content);
        return;

    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme