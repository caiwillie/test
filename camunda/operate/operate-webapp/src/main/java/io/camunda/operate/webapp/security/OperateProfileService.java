/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.core.env.Environment
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
    public static final Set<String> AUTH_PROFILES = Set.of("auth", "ldap-auth", "sso-auth", "identity-auth");
    private static final Set<String> CANT_LOGOUT_AUTH_PROFILES = Set.of("sso-auth");
    @Autowired
    private Environment environment;

    public String getMessageByProfileFor(Exception exception) {
        if (exception == null) return "";
        if (!this.isDevelopmentProfileActive()) return "";
        return exception.getMessage();
    }

    public boolean isDevelopmentProfileActive() {
        return List.of(this.environment.getActiveProfiles()).contains("dev");
    }

    public boolean isSSOProfile() {
        return Arrays.asList(this.environment.getActiveProfiles()).contains(SSO_AUTH_PROFILE);
    }

    public boolean isIdentityProfile() {
        return Arrays.asList(this.environment.getActiveProfiles()).contains(IDENTITY_AUTH_PROFILE);
    }

    public boolean currentProfileCanLogout() {
        return Arrays.stream(this.environment.getActiveProfiles()).noneMatch(CANT_LOGOUT_AUTH_PROFILES::contains);
    }

    public boolean isLoginDelegated() {
        return this.isIdentityProfile() || this.isSSOProfile();
    }
}
