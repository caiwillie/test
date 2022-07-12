package com.brandnewdata.mop.api.dto.protocol.request;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * http request
 */
@Data
public class HttpRequest {
    /**
     * 请求头
     */
    private Map<String, String> headers;

    /**
     * 请求体
     */
    private String body;

    /**
     * 请求参数
     */
    private URLParams params;
}
