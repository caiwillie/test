/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 */
package io.camunda.operate.webapp.api.v1.exceptions;

import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public class ValidationException
extends APIException {
    public static final String TYPE = "Data invalid";

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
