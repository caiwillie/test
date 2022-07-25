package com.brandnewdata.mop.poc.error;

import cn.hutool.core.util.StrUtil;

public class ErrorMessage {

    public static String NOT_NULL(String field) {
        return CHECK_ERROR(StrUtil.format("{} 不能为空", field));
    }

    public static String CHECK_ERROR(String message) {
        return StrUtil.format("【校验错误】 {}", message);
    }

    public static String  STALE_DATA_NOT_EXIST(String field, String value) {
        return StrUtil.format("【失效数据】 {} 不存在数据 {}", field);
    }

}
