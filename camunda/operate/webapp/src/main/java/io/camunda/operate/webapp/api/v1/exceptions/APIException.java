/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.api.v1.exceptions;

import java.util.UUID;

public abstract class APIException
extends RuntimeException {
    private String instance = UUID.randomUUID().toString();

    protected APIException(String message) {
        super(message);
    }

    protected APIException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getInstance() {
        return this.instance;
    }

    public APIException setInstance(String instance) {
        this.instance = instance;
        return this;
    }
}
