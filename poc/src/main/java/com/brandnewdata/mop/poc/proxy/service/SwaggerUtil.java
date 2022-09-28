package com.brandnewdata.mop.poc.proxy.service;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class SwaggerUtil {

    public static void parse(String content) {
        SwaggerParseResult result = new OpenAPIParser().readContents("./path/to/openapi.yaml", null, null);
    }

}
