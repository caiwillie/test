package com.brandnewdata.mop.poc.proxy.bo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ProxyFilter {
    private Long projectId;
    private String name;
    private String version;
    private String tags;
}
