/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
 */
package io.camunda.operate.webapp.security.oauth2;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface JwtAuthenticationTokenValidator {
    public boolean isValid(JwtAuthenticationToken var1);
}
