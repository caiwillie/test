package com.brandnewdata.mop.poc.operate.entity;

import java.time.OffsetDateTime;
import java.util.Map;

public class EventMetadataEntity {
    private String jobType;
    private Integer jobRetries;
    private String jobWorker;
    private OffsetDateTime jobDeadline;
    private Map<String, String> jobCustomHeaders;
    private ErrorType incidentErrorType;
    private String incidentErrorMessage;
    private Long jobKey;
}
