package com.brandnewdata.mop.poc.operate.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ErrorType {
    UNSPECIFIED("Unspecified"),
    UNKNOWN("Unknown"),
    IO_MAPPING_ERROR("I/O mapping error"),
    JOB_NO_RETRIES("No more retries left"),
    CONDITION_ERROR("Condition error"),
    EXTRACT_VALUE_ERROR("Extract value error"),
    CALLED_ELEMENT_ERROR("Called element error"),
    UNHANDLED_ERROR_EVENT("Unhandled error event"),
    MESSAGE_SIZE_EXCEEDED("Message size exceeded"),
    CALLED_DECISION_ERROR("Called decision error"),
    DECISION_EVALUATION_ERROR("Decision evaluation error");

    private static final Logger logger = LoggerFactory.getLogger(ErrorType.class);
    private String title;

    private ErrorType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public static ErrorType fromZeebeErrorType(String errorType) {
        if (errorType == null) {
            return UNSPECIFIED;
        } else {
            try {
                return valueOf(errorType);
            } catch (IllegalArgumentException var2) {
                logger.error("Error type not found for value [{}]. UNKNOWN type will be assigned.", errorType);
                return UNKNOWN;
            }
        }
    }

}
