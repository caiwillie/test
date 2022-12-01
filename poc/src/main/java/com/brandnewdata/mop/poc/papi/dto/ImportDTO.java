package com.brandnewdata.mop.poc.papi.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImportDTO {
    private Proxy proxy;

    private List<Endpoint> endpointList;
}
