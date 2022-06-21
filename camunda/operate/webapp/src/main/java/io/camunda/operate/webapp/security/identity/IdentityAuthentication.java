package io.camunda.operate.webapp.security.identity;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.camunda.identity.sdk.Identity;
import io.camunda.identity.sdk.authentication.AccessToken;
import io.camunda.identity.sdk.authentication.Tokens;
import io.camunda.identity.sdk.authentication.UserDetails;
import io.camunda.identity.sdk.authentication.dto.AuthCodeDto;
import io.camunda.identity.sdk.exception.IdentityException;
import io.camunda.operate.util.RetryOperation;
import io.camunda.operate.webapp.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Component;

@Profile({"identity-auth"})
@Component
@Scope("prototype")
public class IdentityAuthentication extends AbstractAuthenticationToken {
   public static final String READ_PERMISSION_VALUE = "read:*";
   public static final String WRITE_PERMISSION_VALUE = "write:*";
   protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());
   @Autowired
   private transient Identity identity;
   private AccessToken accessToken;
   private Tokens tokens;
   private UserDetails userDetails;

   public IdentityAuthentication() {
      super((Collection)null);
   }

   public String getCredentials() {
      return this.tokens.getAccessToken();
   }

   public Object getPrincipal() {
      return this.accessToken.getToken().getSubject();
   }

   public Tokens getTokens() {
      return this.tokens;
   }

   private boolean hasExpired() {
      Date expires = this.accessToken.getToken().getExpiresAt();
      return expires == null || expires.before(new Date());
   }

   private boolean hasRefreshTokenExpired() {
      DecodedJWT refreshToken = this.identity.authentication().decodeJWT(this.tokens.getRefreshToken());
      Date expires = refreshToken.getExpiresAt();
      return expires == null || expires.before(new Date());
   }

   public String getName() {
      return (String)this.userDetails.getName().orElse("");
   }

   public boolean isAuthenticated() {
      if (this.hasExpired()) {
         this.logger.info("Access token is expired");
         if (this.hasRefreshTokenExpired()) {
            this.setAuthenticated(false);
            this.logger.info("No refresh token available. Authentication is invalid.");
         } else {
            this.logger.info("Get a new access token by using refresh token");

            try {
               this.renewAccessToken();
            } catch (Exception var2) {
               this.logger.error("Renewing access token failed with exception", var2);
               this.setAuthenticated(false);
            }
         }
      }

      return super.isAuthenticated();
   }

   public String getId() {
      return this.userDetails.getId();
   }

   private boolean hasPermission(String permissionName) {
      return this.accessToken.hasPermissions(Set.of(permissionName));
   }

   private boolean hasReadPermission() {
      return this.hasPermission("read:*");
   }

   private boolean hasWritePermission() {
      return this.hasPermission("write:*");
   }

   public List getPermissions() {
      List permissions = new ArrayList();
      if (this.hasReadPermission()) {
         permissions.add(Permission.READ);
      }

      if (this.hasWritePermission()) {
         permissions.add(Permission.WRITE);
      }

      return permissions;
   }

   public void authenticate(HttpServletRequest req, AuthCodeDto authCodeDto) throws Exception {
      this.authenticate(this.retrieveTokens(req, authCodeDto));
   }

   private void authenticate(Tokens tokens) throws Exception {
      this.tokens = tokens;
      this.accessToken = this.identity.authentication().verifyToken(tokens.getAccessToken());
      this.userDetails = this.accessToken.getUserDetails();
      if (!this.getPermissions().contains(Permission.READ)) {
         throw new InsufficientAuthenticationException("No read permissions");
      } else {
         this.setAuthenticated(true);
      }
   }

   private void renewAccessToken() throws Exception {
      this.authenticate(this.renewTokens(this.tokens.getRefreshToken()));
   }

   private Tokens retrieveTokens(HttpServletRequest req, AuthCodeDto authCodeDto) throws Exception {
      return (Tokens)this.requestWithRetry(() -> {
         return this.identity.authentication().exchangeAuthCode(authCodeDto, getRedirectURI(req, "/identity-callback"));
      });
   }

   private Tokens renewTokens(String refreshToken) throws Exception {
      return (Tokens)this.requestWithRetry(() -> {
         return this.identity.authentication().renewToken(refreshToken);
      });
   }

   private Object requestWithRetry(RetryOperation.RetryConsumer retryConsumer) throws Exception {
      return RetryOperation.newBuilder().noOfRetry(10).delayInterval(500, TimeUnit.MILLISECONDS).retryOn(new Class[]{IdentityException.class}).retryConsumer(retryConsumer).build().retry();
   }

   public static String getRedirectURI(HttpServletRequest req, String redirectTo) {
      String var10000 = req.getScheme();
      String redirectUri = var10000 + "://" + req.getServerName();
      if (req.getScheme().equals("http") && req.getServerPort() != 80 || req.getScheme().equals("https") && req.getServerPort() != 443) {
         redirectUri = redirectUri + ":" + req.getServerPort();
      }

      String result;
      if (contextPathIsUUID(req.getContextPath())) {
         String clusterId = req.getContextPath().replace("/", "");
         result = redirectUri + redirectTo + "?uuid=" + clusterId;
      } else {
         result = redirectUri + req.getContextPath() + redirectTo;
      }

      return result;
   }

   protected static boolean contextPathIsUUID(String contextPath) {
      try {
         UUID.fromString(contextPath.replace("/", ""));
         return true;
      } catch (Exception var2) {
         return false;
      }
   }
}
