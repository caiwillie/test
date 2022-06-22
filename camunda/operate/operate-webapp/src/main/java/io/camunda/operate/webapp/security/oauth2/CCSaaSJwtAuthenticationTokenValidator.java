/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.property.OAuthClientProperties
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.webapp.security.oauth2.JwtAuthenticationTokenValidator
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security.oauth2;

import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OAuthClientProperties;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.webapp.security.oauth2.JwtAuthenticationTokenValidator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CCSaaSJwtAuthenticationTokenValidator
implements JwtAuthenticationTokenValidator {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String AUDIENCE = "aud";
    public static final String SCOPE = "scope";
    @Autowired
    private OperateProperties operateProperties;

    public boolean isValid(JwtAuthenticationToken token) {
        Map payload = token.getTokenAttributes();
        return this.isValid(payload);
    }

    private boolean isValid(Map<String, Object> payload) {
        try {
            String audience = this.getAudience(payload);
            String scope = this.getScope(payload);
            OAuthClientProperties clientConfig = this.operateProperties.getClient();
            return clientConfig.getAudience().equals(audience) && clientConfig.getScope().equals(scope);
        }
        catch (Exception e) {
            this.logger.error(String.format("Validation of JWT payload failed due to %s. Request is not authenticated.", e.getMessage()), e);
            return false;
        }
    }

    private String getScope(Map<String, Object> payload) {
        Object scopeObject = payload.get(SCOPE);
        if (scopeObject instanceof String) {
            return (String)scopeObject;
        }
        if (!(scopeObject instanceof List)) throw new OperateRuntimeException(String.format("Couldn't get scope from type %s", scopeObject.getClass()));
        return (String)CollectionUtil.firstOrDefault((List)((List)CollectionUtil.getOrDefaultFromMap(payload, AUDIENCE, Collections.emptyList())), null);
    }

    private String getAudience(Map<String, Object> payload) {
        return (String)CollectionUtil.firstOrDefault((List)((List)CollectionUtil.getOrDefaultFromMap(payload, AUDIENCE, Collections.emptyList())), null);
    }
}
