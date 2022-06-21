package io.camunda.operate.webapp.security;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class OperateProfileService {
   public static final String SSO_AUTH_PROFILE = "sso-auth";
   public static final String IDENTITY_AUTH_PROFILE = "identity-auth";
   public static final String AUTH_PROFILE = "auth";
   public static final String DEFAULT_AUTH = "auth";
   public static final String LDAP_AUTH_PROFILE = "ldap-auth";
   public static final Set AUTH_PROFILES = Set.of("auth", "ldap-auth", "sso-auth", "identity-auth");
   private static final Set CANT_LOGOUT_AUTH_PROFILES = Set.of("sso-auth");
   @Autowired
   private Environment environment;

   public String getMessageByProfileFor(Exception exception) {
      return exception != null && this.isDevelopmentProfileActive() ? exception.getMessage() : "";
   }

   public boolean isDevelopmentProfileActive() {
      return List.of(this.environment.getActiveProfiles()).contains("dev");
   }

   public boolean isSSOProfile() {
      return Arrays.asList(this.environment.getActiveProfiles()).contains("sso-auth");
   }

   public boolean isIdentityProfile() {
      return Arrays.asList(this.environment.getActiveProfiles()).contains("identity-auth");
   }

   public boolean currentProfileCanLogout() {
      Stream var10000 = Arrays.stream(this.environment.getActiveProfiles());
      Set var10001 = CANT_LOGOUT_AUTH_PROFILES;
      Objects.requireNonNull(var10001);
      return var10000.noneMatch(var10001::contains);
   }

   public boolean isLoginDelegated() {
      return this.isIdentityProfile() || this.isSSOProfile();
   }
}
