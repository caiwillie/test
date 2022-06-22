/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.security.oauth2.JwtAuthenticationTokenValidator
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.security.authentication.AbstractAuthenticationToken
 *  org.springframework.security.oauth2.jwt.Jwt
 *  org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
 *  org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security.oauth2;

import io.camunda.operate.webapp.security.oauth2.JwtAuthenticationTokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class Jwt2AuthenticationTokenConverter
implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
    @Autowired
    private JwtAuthenticationTokenValidator validator;

    public AbstractAuthenticationToken convert(Jwt jwt) {
        JwtAuthenticationToken token = (JwtAuthenticationToken)this.delegate.convert(jwt);
        if (!this.validator.isValid(token)) return null;
        return token;
    }
}
