package com.brandnewdata.mop.api.dto.protocol.response;

import lombok.Data;

import java.util.Map;

/**
 * http response
 */
@Data
public class HttpResponse {

    /**
     * 响应头
     */
    private Map<String, String> headers;

    /**
     * 响应头
     */
    private String body;
}
