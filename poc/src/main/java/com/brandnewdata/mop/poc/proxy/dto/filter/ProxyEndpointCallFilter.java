package com.brandnewdata.mop.poc.proxy.dto.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class ProxyEndpointCallFilter {

    private LocalDateTime minStartTime;

    private LocalDateTime maxStartTime;
}
