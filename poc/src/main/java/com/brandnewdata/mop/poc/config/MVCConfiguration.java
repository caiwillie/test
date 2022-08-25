package com.brandnewdata.mop.poc.config;


import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MVCConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtil.getObjectMapper();
    }
}
