/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 */
package io.camunda.operate.webapp.rest.exception;

import io.camunda.operate.webapp.rest.exception.NotFoundException;

public class UserNotFoundException
extends NotFoundException {
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
