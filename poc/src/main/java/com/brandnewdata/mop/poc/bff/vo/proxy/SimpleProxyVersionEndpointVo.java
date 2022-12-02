package com.brandnewdata.mop.poc.bff.vo.proxy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleProxyVersionEndpointVo {

    /**
     * endpoint id
     */
    private Long endpointId;

    /**
     * 路径
     */
    private String location;
}
