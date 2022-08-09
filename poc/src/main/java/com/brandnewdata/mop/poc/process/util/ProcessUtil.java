package com.brandnewdata.mop.poc.process.util;

import cn.hutool.core.util.StrUtil;

public class ProcessUtil {

    public static String convertProcessId(String id) {
        String result = null;
        // . 和 :
        result = StrUtil.replace(id, ".", "_");
        result = StrUtil.replace(id, ":", "__");
        return result;
    }

}
