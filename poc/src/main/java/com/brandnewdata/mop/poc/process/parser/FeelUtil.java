package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.map.MapUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule$;
import lombok.SneakyThrows;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.impl.SpiServiceLoader;
import scala.util.Either;

import java.util.Map;
import java.util.Optional;

public class FeelUtil {

    // FeelVariables 可以转换表达式为 null
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new FeelVariablesJsonFactory());

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
        // 使用objectMapper需未注册scala模块
        return JacksonUtil.fromMap(str);
    }
    @SneakyThrows
    public static <T> T convertValue(Object obj, Class<T> valueType) {
        if (obj == null) return null;
        String str = OBJECT_MAPPER.writeValueAsString(obj);
        // 使用objectMapper需未注册scala模块
        return JacksonUtil.from(str, valueType);
    }

}
