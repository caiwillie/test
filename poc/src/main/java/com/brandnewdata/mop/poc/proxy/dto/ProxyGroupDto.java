package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProxyGroupDto {
    private String name;

    private List<ProxyDto> proxyDtoList;
}
