package com.brandnewdata.mop.poc.operate.po;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
public class EventMetadataPo {
    private String jobType;
    private Integer jobRetries;
    private String jobWorker;
    private OffsetDateTime jobDeadline;
    private Map<String, String> jobCustomHeaders;
    private ErrorType incidentErrorType;
    private String incidentErrorMessage;
    private Long jobKey;
}
