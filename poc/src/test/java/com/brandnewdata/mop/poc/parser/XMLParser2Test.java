package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class XMLParser2Test {

    @Test
    void parse() {
        String content = ResourceUtil.readStr("mobile.bpmn.xml", StandardCharsets.UTF_8);
        XMLDTO parse = new XMLParser2().parse(content);
        return;

    }

}
