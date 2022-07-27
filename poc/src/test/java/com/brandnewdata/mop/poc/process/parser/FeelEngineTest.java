package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.map.MapUtil;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.impl.SpiServiceLoader;
import org.junit.jupiter.api.Test;
import scala.util.Either;

import java.util.Map;

public class FeelEngineTest {

    public static void main(String[] args) {

        final FeelEngine engine = new FeelEngine.Builder()
                .valueMapper(SpiServiceLoader.loadValueMapper())
                .functionProvider(SpiServiceLoader.loadFunctionProvider())
                .build();

        final Map<String, Object> variables = MapUtil.of("x", 21);
        final Either<FeelEngine.Failure, Object> result = engine.evalExpression("x + 1", variables);

        if (result.isRight()) {
            final Object value = result.right().get();
            System.out.println("result is " + value);
        } else {
            final FeelEngine.Failure failure = result.left().get();
            throw new RuntimeException(failure.message());
        }
    }

    @Test
    public void test() {
        ObjectMapper objectMapper = new ObjectMapper(new FeelVariablesJsonFactory());
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.putPOJO("a", new RawValue("\"a\""));
        objectNode.putPOJO("b", new RawValue("bb"));
        objectNode.putPOJO("c", new RawValue("\"c\""));
        try {
            String s = objectMapper.writeValueAsString(objectNode);
            JsonNode jsonNode = objectMapper.readTree(s);
            System.out.println(s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
