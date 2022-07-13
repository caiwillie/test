package com.brandnewdata.mop.api.dto.protocol.request;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class URLParams {
    /**
     * path参数
     */
    private Map<String, String> path = new HashMap<>();

    /**
     * query参数
     */
    private Map<String, String> query = new HashMap<>();
}
