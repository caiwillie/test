package com.brandnewdata.mop.poc.service;

import cn.hutool.core.util.StrUtil;

public class ServiceUtil {

    public static String convertModelKey(String modelKey) {
        // modelKey中替换.
        modelKey = StrUtil.replace(modelKey, ".", "_");
        modelKey = StrUtil.replace(modelKey, ":", "__");
        return modelKey;
    }
}
