package io.camunda.operate.webapp.security.sso;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.net.TokenRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Component;

@Profile({"sso-auth"})
@Component
@Scope("prototype")
public class TokenAuthentication extends AbstractAuthenticationToken {
   private transient Logger logger = LoggerFactory.getLogger(this.getClass());
   @Value("${camunda.operate.auth0.claimName}")
   private String claimName;
   @Value("${camunda.operate.cloud.organizationid}")
   private String organization;
   @Value("${camunda.operate.auth0.backendDomain}")
   private String domain;
   @Value("${camunda.operate.auth0.clientId}")
   private String clientId;
   @Value("${camunda.operate.auth0.clientSecret}")
   private String clientSecret;
   private String idToken;
   private String refreshToken;
   private List permissions = new ArrayList();

   public TokenAuthentication() {
      super((Collection)null);
   }

   private boolean isIdEqualsOrganization(Map orgs) {
      return orgs.containsKey("id") && ((String)orgs.get("id")).equals(this.organization);
   }

   private Logger getLogger() {
      if (this.logger == null) {
         this.logger = LoggerFactory.getLogger(this.getClass());
      }

      return this.logger;
   }

   public boolean isAuthenticated() {
      if (this.hasExpired()) {
         this.getLogger().info("Access token is expired");
         if (this.refreshToken == null) {
            this.setAuthenticated(false);
            this.getLogger().info("No refresh token available. Authentication is invalid.");
         } else {
            this.getLogger().info("Get a new access token by using refresh token");
            this.getNewTokenByRefreshToken();
         }
      }

      return super.isAuthenticated();
   }

   public List getPermissions() {
      return this.permissions;
   }

   private void getNewTokenByRefreshToken() {
      try {
         TokenRequest tokenRequest = this.getAuthAPI().renewAuth(this.refreshToken);
         TokenHolder tokenHolder = (TokenHolder)tokenRequest.execute();
         this.authenticate(tokenHolder.getIdToken(), tokenHolder.getRefreshToken());
         this.getLogger().info("New access token received and validated.");
      } catch (Auth0Exception var3) {
         this.getLogger().error(var3.getMessage(), var3.getCause());
         this.setAuthenticated(false);
      }

   }

   private AuthAPI getAuthAPI() {
      return new AuthAPI(this.domain, this.clientId, this.clientSecret);
   }

   public boolean hasExpired() {
      Date expires = JWT.decode(this.idToken).getExpiresAt();
      return expires == null || expires.before(new Date());
   }

   public Date getExpiresAt() {
      return JWT.decode(this.idToken).getExpiresAt();
   }

   public String getCredentials() {
      return JWT.decode(this.idToken).getToken();
   }

   public Object getPrincipal() {
      return JWT.decode(this.idToken).getSubject();
   }

   public void authenticate(String idToken, String refreshToken) {
      this.idToken = idToken;
      if (refreshToken != null) {
         this.refreshToken = refreshToken;
      }

      Claim claim = JWT.decode(idToken).getClaim(this.claimName);
      this.tryAuthenticateAsListOfMaps(claim);
      if (!this.isAuthenticated()) {
         throw new InsufficientAuthenticationException("No permission for operate - check your organization id");
      }
   }

   private void tryAuthenticateAsListOfMaps(Claim claim) {
      try {
         List claims = claim.asList(Map.class);
         if (claims != null) {
            this.setAuthenticated(claims.stream().anyMatch(this::isIdEqualsOrganization));
         }
      } catch (JWTDecodeException var3) {
         this.getLogger().debug("Read organization claim as list of maps failed.", var3);
      }

   }

   public Map getClaims() {
      return JWT.decode(this.idToken).getClaims();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            TokenAuthentication that = (TokenAuthentication)o;
            return this.claimName.equals(that.claimName) && this.organization.equals(that.organization) && this.domain.equals(that.domain) && this.clientId.equals(that.clientId) && this.clientSecret.equals(that.clientSecret) && this.idToken.equals(that.idToken) && Objects.equals(this.refreshToken, that.refreshToken);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.claimName, this.organization, this.domain, this.clientId, this.clientSecret, this.idToken, this.refreshToken});
   }
}
