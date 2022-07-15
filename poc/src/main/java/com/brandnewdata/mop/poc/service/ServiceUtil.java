package com.brandnewdata.mop.poc.service;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import java.util.Map;

public class ServiceUtil {


    public static final ObjectMapper OM = new ObjectMapper();

    public static final MapType MAP_TYPE =
            OM.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

    static {
        OM.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static String convertModelKey(String modelKey) {

        // modelKey中替换.
        modelKey = StrUtil.replace(modelKey, ".", "_");
        modelKey = StrUtil.replace(modelKey, ":", "__");
        return modelKey;
    }
}
