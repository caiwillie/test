package com.brandnewdata.mop.poc.config;


import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MVCConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = JacksonUtil.initMapper();
        // 字符串反序列化时自动trim
        objectMapper.registerModule(new StringTrimModule());
        return objectMapper;
    }


}

class StringTrimModule extends SimpleModule {

    public StringTrimModule() {
        addDeserializer(String.class, new StdScalarDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException, JsonProcessingException {
                return jsonParser.getValueAsString().trim();
            }
        });
    }
}
