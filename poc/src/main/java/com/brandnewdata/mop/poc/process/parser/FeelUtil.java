package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.module.scala.DefaultScalaModule$;
import lombok.SneakyThrows;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.impl.SpiServiceLoader;
import scala.util.Either;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FeelUtil {

    // FeelVariables 可以转换表达式为 null
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new FeelVariablesJsonFactory());

    private static final MapType MAP_TYPE = OBJECT_MAPPER.getTypeFactory()
            .constructMapType(Map.class, String.class, Object.class);


    static {
        // 注册 scala 模块
        OBJECT_MAPPER.registerModule(DefaultScalaModule$.MODULE$);
    }

    private static final FeelEngine ENGINE = new FeelEngine.Builder()
            .valueMapper(SpiServiceLoader.loadValueMapper())
            .functionProvider(SpiServiceLoader.loadFunctionProvider())
            .build();

    public static Object evalExpression(String expression, Map<String, Object> values) {

        Either<FeelEngine.Failure, Object> result = ENGINE.evalExpression(expression,
                Optional.ofNullable(values).orElse(MapUtil.empty()));

        if (result.isRight()) {
            return result.right().get();
        } else {
            final FeelEngine.Failure failure = result.left().get();
            throw new RuntimeException(failure.message());
        }
    }

    @SneakyThrows
    public static Map<String, Object> convertMap(Object obj) {
        if (obj == null) return null;
        String str = OBJECT_MAPPER.writeValueAsString(obj);
        return OBJECT_MAPPER.readValue(str, MAP_TYPE);
    }
    @SneakyThrows
    public static <T> T convertValue(Object obj, Class<T> toValueType) {
        if (obj == null) return null;
        String str = OBJECT_MAPPER.writeValueAsString(obj);
        return OBJECT_MAPPER.readValue(str, toValueType);
    }

}
