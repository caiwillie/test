package com.brandnewdata.mop.poc.process.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProcessReleaseDeployDto {
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long envId;

    private String processId;

    private Long processZeebeKey;

    private Integer processZeebeVersion;

    private String processZeebeXml;
}
