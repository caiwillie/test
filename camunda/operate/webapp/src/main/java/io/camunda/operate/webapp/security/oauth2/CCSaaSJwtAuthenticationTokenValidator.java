package io.camunda.operate.webapp.security.oauth2;

import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OAuthClientProperties;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.CollectionUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CCSaaSJwtAuthenticationTokenValidator implements JwtAuthenticationTokenValidator {
   protected final Logger logger = LoggerFactory.getLogger(this.getClass());
   public static final String AUDIENCE = "aud";
   public static final String SCOPE = "scope";
   @Autowired
   private OperateProperties operateProperties;

   public boolean isValid(JwtAuthenticationToken token) {
      Map payload = token.getTokenAttributes();
      return this.isValid(payload);
   }

   private boolean isValid(Map payload) {
      try {
         String audience = this.getAudience(payload);
         String scope = this.getScope(payload);
         OAuthClientProperties clientConfig = this.operateProperties.getClient();
         return clientConfig.getAudience().equals(audience) && clientConfig.getScope().equals(scope);
      } catch (Exception var5) {
         this.logger.error(String.format("Validation of JWT payload failed due to %s. Request is not authenticated.", var5.getMessage()), var5);
         return false;
      }
   }

   private String getScope(Map payload) {
      Object scopeObject = payload.get("scope");
      if (scopeObject instanceof String) {
         return (String)scopeObject;
      } else if (scopeObject instanceof List) {
         return (String)CollectionUtil.firstOrDefault((List)CollectionUtil.getOrDefaultFromMap(payload, "aud", Collections.emptyList()), (Object)null);
      } else {
         throw new OperateRuntimeException(String.format("Couldn't get scope from type %s", scopeObject.getClass()));
      }
   }

   private String getAudience(Map payload) {
      return (String)CollectionUtil.firstOrDefault((List)CollectionUtil.getOrDefaultFromMap(payload, "aud", Collections.emptyList()), (Object)null);
   }
}
