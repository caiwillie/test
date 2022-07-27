package com.brandnewdata.mop.poc.process.dto;

import lombok.Data;

@Data
public class ProcessInstance {

    private String processId;

    private String version;

    private String instanceId;

    private String parentInstanceId;

    private String startTime;

    private String endTime;
}
