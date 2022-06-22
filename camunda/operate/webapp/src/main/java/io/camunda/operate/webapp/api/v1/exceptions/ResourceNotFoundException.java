/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.exceptions.APIException
 */
package io.camunda.operate.webapp.api.v1.exceptions;

import io.camunda.operate.webapp.api.v1.exceptions.APIException;

public class ResourceNotFoundException
extends APIException {
    public static final String TYPE = "Requested resource not found";

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
