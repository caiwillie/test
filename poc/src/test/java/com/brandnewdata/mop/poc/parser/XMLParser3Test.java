package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.io.resource.ResourceUtil;
import com.brandnewdata.mop.poc.common.Constants;
import com.brandnewdata.mop.poc.service.ModelService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@SpringBootTest
public class XMLParser3Test {

    @Resource
    private ModelService modelService;

    @Test
    void parse() {
        String json = ResourceUtil.readStr("test.json", StandardCharsets.UTF_8);
        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode jsonNode = om.readTree(json);
            String xml = jsonNode.get(0).get("processEditing").textValue();
            XMLDTO xmldto = new XMLParser3()
                    .parse(xml)
                    .replaceGeneralTrigger()
                    .build();
            modelService.deploy(xmldto.getModelKey(), xmldto.getName(), xmldto.getZeebeXML(), Constants.TRIGGER_TYPE_NONE);
            return;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void startGeneralTrigger() {
        String json = ResourceUtil.readStr("test.json", StandardCharsets.UTF_8);
        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode jsonNode = om.readTree(json);
            String xml = jsonNode.get(0).get("processEditing").textValue();
            XMLDTO xmldto = new XMLParser3()
                    .parse(xml)
                    .replaceGeneralTrigger()
                    .build();

            return;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void start() {
        modelService.start("Process_0298f5ac-b918-44ba-838c-b88b52c5ba5f", new HashMap<>());
        return;
    }

    @Test
    void sendMessage() {
        modelService.sendMessage("Process_0298f5ac-b918-44ba-838c-b88b52c5ba5f", new HashMap<>());
        return;
    }

}
