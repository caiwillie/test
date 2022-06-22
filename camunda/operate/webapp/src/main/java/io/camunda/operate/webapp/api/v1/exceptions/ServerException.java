/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 */
package io.camunda.operate.webapp.api.v1.exceptions;

import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public class ServerException
extends APIException {
    public static final String TYPE = "API application error";

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerException(String message) {
        super(message);
    }
}
