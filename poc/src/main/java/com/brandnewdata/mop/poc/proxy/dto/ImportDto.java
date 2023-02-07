package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImportDto {

    private ProxyDto proxy;

    private List<ProxyEndpointDto> endpointList;

}
