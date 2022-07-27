package com.brandnewdata.mop.poc.service;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import java.util.Map;

public class ServiceUtil {
    
    public static String convertModelKey(String modelKey) {

        // modelKey中替换.
        modelKey = StrUtil.replace(modelKey, ".", "_");
        modelKey = StrUtil.replace(modelKey, ":", "__");
        return modelKey;
    }
}
