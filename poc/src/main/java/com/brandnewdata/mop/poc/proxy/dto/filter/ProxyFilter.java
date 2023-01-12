package com.brandnewdata.mop.poc.proxy.dto.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class ProxyFilter {
    private Long projectId;
    private String name;
    private String version;
    private String tags;
    private LocalDateTime minStartTime;
    private LocalDateTime maxStartTime;
}
