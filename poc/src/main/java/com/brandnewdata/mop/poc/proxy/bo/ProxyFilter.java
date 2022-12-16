package com.brandnewdata.mop.poc.proxy.bo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ProxyFilter {
    private String name;
    private String version;
    private String tags;
}
