package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class XMLParserTest {



    @Test
    void parse() {
        String content = ResourceUtil.readStr("demo.bpmn.xml", StandardCharsets.UTF_8);
        XMLDTO parse = new XMLParser().parse(content);
        return;

    }

    @Test
    void test() {
        Map map = new HashMap();
        map.put("a", "\"\"\"");
        String s = JSONUtil.toJsonStr(map);
        System.out.println(s);

        JSONObject entries = JSONUtil.parseObj(s);
        return;
    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme