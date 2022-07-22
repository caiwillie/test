package com.brandnewdata.mop.poc.error;

import cn.hutool.core.util.StrUtil;

public class ErrorMessage {

    public static String NOT_NULL(String field) {
        return StrUtil.format("{} 不能为空", field);
    }



}
