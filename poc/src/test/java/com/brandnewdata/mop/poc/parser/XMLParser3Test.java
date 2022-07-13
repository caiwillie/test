package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class XMLParser3Test {

    @Test
    void parse() {
        String content = ResourceUtil.readStr("v3.bpmn.xml", StandardCharsets.UTF_8);
        XMLDTO parse = new XMLParser3().parse(content);
        /*
        ObjectMapper objectMapper = new ObjectMapper();


        Map<String, Object> map = new HashMap<>();
        map.put("a", new RawString("b"));
        try {

            String value = objectMapper.writeValueAsString(map);
            return;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        */
    }


    public class RawString implements JsonSerializable {

        private String value;

        public RawString(String value) {
            this.value = value;
        }

        @Override
        public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeRawValue(value);
        }

        @Override
        public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
            serialize(gen, serializers);
        }
    }

}
