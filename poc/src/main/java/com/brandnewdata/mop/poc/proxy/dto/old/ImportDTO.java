package com.brandnewdata.mop.poc.proxy.dto.old;

import lombok.Data;

import java.util.List;

@Data
public class ImportDTO {
    private Proxy proxy;

    private List<Endpoint> endpointList;
}
