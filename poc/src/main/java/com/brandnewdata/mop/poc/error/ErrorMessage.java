package com.brandnewdata.mop.poc.error;

import cn.hutool.core.util.StrUtil;

public class ErrorMessage {

    public static String NOT_NULL(String field) {
        return StrUtil.format("【校验错误】 {} 不能为空", field);
    }

    public static String  STALE_DATA_NOT_EXIST(String field, String value) {
        return StrUtil.format("【失效数据】 {} 不存在数据 {}", field);
    }

}
