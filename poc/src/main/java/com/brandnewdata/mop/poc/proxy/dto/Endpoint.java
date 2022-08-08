package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Data;

@Data
public class Endpoint {

    /**
     * id
     */
    private Long id;

    /**
     * api id
     */
    private Long proxyId;

    /**
     * 位置
     */
    private String location;
}
