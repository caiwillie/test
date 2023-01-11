package com.brandnewdata.mop.poc.proxy.dto.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ProxyEndpointFilter {
    private String location;
}
