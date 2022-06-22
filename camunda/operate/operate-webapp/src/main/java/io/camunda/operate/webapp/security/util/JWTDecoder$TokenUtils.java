/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.auth0.jwt.exceptions.JWTDecodeException
 */
package io.camunda.operate.webapp.security.util;

import com.auth0.jwt.exceptions.JWTDecodeException;
/*

static abstract class TokenUtils {
    TokenUtils() {
    }

    static String[] splitToken(String token) throws JWTDecodeException {
        String[] parts = token.split("\\.");
        if (parts.length == 2 && token.endsWith(".")) {
            parts = new String[]{parts[0], parts[1], ""};
        }
        if (parts.length == 3) return parts;
        throw new JWTDecodeException(String.format("The token was expected to have 3 parts, but got %s.", parts.length));
    }
}
*/
