package io.camunda.operate.webapp.security.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

@Component
public class OAuth2WebConfigurer {
   public static final String SPRING_SECURITY_OAUTH_2_RESOURCESERVER_JWT_ISSUER_URI = "spring.security.oauth2.resourceserver.jwt.issuer-uri";
   public static final String SPRING_SECURITY_OAUTH_2_RESOURCESERVER_JWT_JWK_SET_URI = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri";
   private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2WebConfigurer.class);
   @Autowired
   private Environment env;
   @Autowired
   private Jwt2AuthenticationTokenConverter jwtConverter;

   public void configure(HttpSecurity http) throws Exception {
      if (this.isJWTEnabled()) {
         http.oauth2ResourceServer((serverCustomizer) -> {
            serverCustomizer.jwt((jwtCustomizer) -> {
               jwtCustomizer.jwtAuthenticationConverter(this.jwtConverter);
            });
         });
         LOGGER.info("Enabled OAuth2 JWT access to Operate API");
      }

   }

   protected boolean isJWTEnabled() {
      return this.env.containsProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri") || this.env.containsProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
   }
}
