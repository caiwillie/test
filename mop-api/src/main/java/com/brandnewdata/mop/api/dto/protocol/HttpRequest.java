package com.brandnewdata.mop.api.dto.protocol;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HttpRequest {
    /**
     * 请求头
     */
    private Map<String, String> headers=new HashMap<>();

    /**
     * 请求体
     */
    private String body;

    /**
     * 请求参数
     */
    private URLParams params;
}
