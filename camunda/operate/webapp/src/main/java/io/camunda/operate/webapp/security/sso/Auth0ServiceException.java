/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.security.sso;

public class Auth0ServiceException
extends RuntimeException {
    public Auth0ServiceException(String message) {
        super(message);
    }

    public Auth0ServiceException(Exception e) {
        super(e);
    }
}
