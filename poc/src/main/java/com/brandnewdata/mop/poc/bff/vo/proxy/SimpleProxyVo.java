package com.brandnewdata.mop.poc.bff.vo.proxy;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimpleProxyVo {

    /**
     * 版本号
     */
    private String version;

    /**
     * 路径列表
     */
    private List<SimpleProxyEndpointVo> endpointList;
}
