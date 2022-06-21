package io.camunda.operate.webapp.security.oauth2;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface JwtAuthenticationTokenValidator {
   boolean isValid(JwtAuthenticationToken var1);
}
