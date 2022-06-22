/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 */
package io.camunda.operate.webapp.api.v1.exceptions;

import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public class ClientException
extends APIException {
    public static final String TYPE = "Invalid request";

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
