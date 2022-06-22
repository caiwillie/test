/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.auth0.client.auth.AuthAPI
 *  com.auth0.exception.Auth0Exception
 *  com.auth0.json.auth.TokenHolder
 *  com.auth0.jwt.JWT
 *  com.auth0.jwt.exceptions.JWTDecodeException
 *  com.auth0.jwt.interfaces.Claim
 *  com.auth0.net.TokenRequest
 *  io.camunda.operate.webapp.security.Permission
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Profile
 *  org.springframework.context.annotation.Scope
 *  org.springframework.security.authentication.AbstractAuthenticationToken
 *  org.springframework.security.authentication.InsufficientAuthenticationException
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security.sso;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.net.TokenRequest;
import io.camunda.operate.webapp.security.Permission;
import java.util.ArrayList;
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

@Profile(value={"sso-auth"})
@Component
@Scope(value="prototype")
public class TokenAuthentication extends AbstractAuthenticationToken {
    private transient Logger logger = LoggerFactory.getLogger(((Object)((Object)this)).getClass());
    @Value(value="${camunda.operate.auth0.claimName}")
    private String claimName;
    @Value(value="${camunda.operate.cloud.organizationid}")
    private String organization;
    @Value(value="${camunda.operate.auth0.backendDomain}")
    private String domain;
    @Value(value="${camunda.operate.auth0.clientId}")
    private String clientId;
    @Value(value="${camunda.operate.auth0.clientSecret}")
    private String clientSecret;
    private String idToken;
    private String refreshToken;
    private List<Permission> permissions = new ArrayList<Permission>();

    public TokenAuthentication() {
        super(null);
    }

    private boolean isIdEqualsOrganization(Map<String, String> orgs) {
        return orgs.containsKey("id") && orgs.get("id").equals(this.organization);
    }

    private Logger getLogger() {
        if (this.logger != null) return this.logger;
        this.logger = LoggerFactory.getLogger(((Object)((Object)this)).getClass());
        return this.logger;
    }

    public boolean isAuthenticated() {
        if (!this.hasExpired()) return super.isAuthenticated();
        this.getLogger().info("Access token is expired");
        if (this.refreshToken == null) {
            this.setAuthenticated(false);
            this.getLogger().info("No refresh token available. Authentication is invalid.");
        } else {
            this.getLogger().info("Get a new access token by using refresh token");
            this.getNewTokenByRefreshToken();
        }
        return super.isAuthenticated();
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }

    private void getNewTokenByRefreshToken() {
        try {
            TokenRequest tokenRequest = this.getAuthAPI().renewAuth(this.refreshToken);
            TokenHolder tokenHolder = (TokenHolder)tokenRequest.execute();
            this.authenticate(tokenHolder.getIdToken(), tokenHolder.getRefreshToken());
            this.getLogger().info("New access token received and validated.");
        }
        catch (Auth0Exception e) {
            this.getLogger().error(e.getMessage(), e.getCause());
            this.setAuthenticated(false);
        }
    }

    private AuthAPI getAuthAPI() {
        return new AuthAPI(this.domain, this.clientId, this.clientSecret);
    }

    public boolean hasExpired() {
        Date expires = JWT.decode((String)this.idToken).getExpiresAt();
        return expires == null || expires.before(new Date());
    }

    public Date getExpiresAt() {
        return JWT.decode((String)this.idToken).getExpiresAt();
    }

    public String getCredentials() {
        return JWT.decode((String)this.idToken).getToken();
    }

    public Object getPrincipal() {
        return JWT.decode((String)this.idToken).getSubject();
    }

    public void authenticate(String idToken, String refreshToken) {
        this.idToken = idToken;
        if (refreshToken != null) {
            this.refreshToken = refreshToken;
        }
        Claim claim = JWT.decode((String)idToken).getClaim(this.claimName);
        this.tryAuthenticateAsListOfMaps(claim);
        if (this.isAuthenticated()) return;
        throw new InsufficientAuthenticationException("No permission for operate - check your organization id");
    }

    private void tryAuthenticateAsListOfMaps(Claim claim) {
        try {
            List<Map> claims = claim.asList(Map.class);
            if (claims == null) return;
            this.setAuthenticated(claims.stream().anyMatch(this::isIdEqualsOrganization));
        }
        catch (JWTDecodeException e) {
            this.getLogger().debug("Read organization claim as list of maps failed.", e);
        }
    }

    public Map<String, Claim> getClaims() {
        return JWT.decode((String)this.idToken).getClaims();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (((Object)((Object)this)).getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TokenAuthentication that = (TokenAuthentication)((Object)o);
        return this.claimName.equals(that.claimName) && this.organization.equals(that.organization) && this.domain.equals(that.domain) && this.clientId.equals(that.clientId) && this.clientSecret.equals(that.clientSecret) && this.idToken.equals(that.idToken) && Objects.equals(this.refreshToken, that.refreshToken);
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), this.claimName, this.organization, this.domain, this.clientId, this.clientSecret, this.idToken, this.refreshToken);
    }
}
