package io.camunda.operate.webapp.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class Jwt2AuthenticationTokenConverter implements Converter {
   private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
   @Autowired
   private JwtAuthenticationTokenValidator validator;

   public AbstractAuthenticationToken convert(Jwt jwt) {
      JwtAuthenticationToken token = (JwtAuthenticationToken)this.delegate.convert(jwt);
      return this.validator.isValid(token) ? token : null;
   }
}
