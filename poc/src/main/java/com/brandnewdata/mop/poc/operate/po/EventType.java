package com.brandnewdata.mop.poc.operate.po;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum EventType {
    CREATED,
    RESOLVED,
    SEQUENCE_FLOW_TAKEN,
    ELEMENT_ACTIVATING,
    ELEMENT_ACTIVATED,
    ELEMENT_COMPLETING,
    ELEMENT_COMPLETED,
    ELEMENT_TERMINATED,
    ACTIVATED,
    COMPLETED,
    TIMED_OUT,
    FAILED,
    RETRIES_UPDATED,
    CANCELED,
    UNKNOWN;

    private static final Logger logger = LoggerFactory.getLogger(EventType.class);

    private EventType() {
    }

    public static EventType fromZeebeIntent(String intent) {
        try {
            return valueOf(intent);
        } catch (IllegalArgumentException var2) {
            logger.error("Event type not found for value [{}]. UNKNOWN type will be assigned.", intent);
            return UNKNOWN;
        }
    }
}
