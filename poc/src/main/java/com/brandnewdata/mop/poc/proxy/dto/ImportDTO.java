package com.brandnewdata.mop.poc.proxy.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImportDTO {
    private Proxy proxy;

    private List<Endpoint> endpointList;
}
