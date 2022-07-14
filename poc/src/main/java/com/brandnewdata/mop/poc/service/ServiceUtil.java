package com.brandnewdata.mop.poc.service;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceUtil {
    private static final ObjectMapper OM = new ObjectMapper();

    public static String convertModelKey(String modelKey) {
        // modelKey中替换.
        modelKey = StrUtil.replace(modelKey, ".", "_");
        modelKey = StrUtil.replace(modelKey, ":", "__");
        return modelKey;
    }
}
