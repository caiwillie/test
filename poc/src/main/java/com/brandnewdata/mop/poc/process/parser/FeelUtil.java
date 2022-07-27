package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.MapType;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.impl.SpiServiceLoader;
import scala.util.Either;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FeelUtil {



    private static final FeelEngine ENGINE = new FeelEngine.Builder()
            .valueMapper(SpiServiceLoader.loadValueMapper())
            .functionProvider(SpiServiceLoader.loadFunctionProvider())
            .build();

    public static Object evalExpression(String expression, Map<String, Object> values) {

        Either<FeelEngine.Failure, Object> result = ENGINE.evalExpression(expression,
                Optional.ofNullable(values).orElse(MapUtil.empty()));

        if (result.isRight()) {
            Object value = result.right().get();
            return value;
        } else {
            final FeelEngine.Failure failure = result.left().get();
            throw new RuntimeException(failure.message());
        }
    }

}
