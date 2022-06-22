/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.auth0.AuthenticationController
 *  com.auth0.IdentityVerificationException
 *  com.auth0.Tokens
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.util.RetryOperation
 *  io.camunda.operate.webapp.security.Permission
 *  io.camunda.operate.webapp.security.sso.Auth0ServiceException
 *  io.camunda.operate.webapp.security.sso.TokenAuthentication
 *  io.camunda.operate.webapp.security.sso.model.ClusterInfo
 *  io.camunda.operate.webapp.security.sso.model.ClusterInfo$Permission
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.springframework.beans.factory.BeanFactory
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.boot.web.client.RestTemplateBuilder
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Profile
 *  org.springframework.http.HttpEntity
 *  org.springframework.http.HttpHeaders
 *  org.springframework.http.HttpMethod
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.authentication.InsufficientAuthenticationException
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.stereotype.Component
 *  org.springframework.util.MultiValueMap
 *  org.springframework.web.client.RestTemplate
 */
package io.camunda.operate.webapp.security.sso;

import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.Tokens;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.RetryOperation;
import io.camunda.operate.webapp.security.Permission;
import io.camunda.operate.webapp.security.sso.Auth0ServiceException;
import io.camunda.operate.webapp.security.sso.TokenAuthentication;
import io.camunda.operate.webapp.security.sso.model.ClusterInfo;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Profile(value={"sso-auth"})
public class Auth0Service {
    private static final String LOGOUT_URL_TEMPLATE = "https://%s/v2/logout?client_id=%s&returnTo=%s";
    private static final String PERMISSION_URL_TEMPLATE = "%s/%s";
    private static final List<String> SCOPES = List.of("openid", "profile", "email", "offline_access");
    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private AuthenticationController authenticationController;
    @Value(value="${camunda.operate.auth0.domain}")
    private String domain;
    @Value(value="${camunda.operate.auth0.backendDomain}")
    private String backendDomain;
    @Value(value="${camunda.operate.auth0.clientId}")
    private String clientId;
    @Autowired
    private OperateProperties operateProperties;
    @Autowired
    private RestTemplateBuilder builder;
    @Autowired
    @Qualifier(value="auth0_restTemplate")
    private RestTemplate restTemplate;

    @Bean(value={"auth0_restTemplate"})
    public RestTemplate restTemplate() {
        return this.builder.build();
    }

    public void authenticate(HttpServletRequest req, HttpServletResponse res) throws Auth0ServiceException {
        try {
            Tokens tokens = this.retrieveTokens(req, res);
            TokenAuthentication authentication = (TokenAuthentication)this.beanFactory.getBean(TokenAuthentication.class);
            this.checkPermission(authentication, tokens.getAccessToken());
            authentication.authenticate(tokens.getIdToken(), tokens.getRefreshToken());
            SecurityContextHolder.getContext().setAuthentication((Authentication)authentication);
            this.sessionExpiresWhenAuthenticationExpires(req);
        }
        catch (Exception e) {
            throw new Auth0ServiceException(e);
        }
    }

    private void checkPermission(TokenAuthentication authentication, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        String urlDomain = this.operateProperties.getCloud().getPermissionUrl();
        String url = String.format(PERMISSION_URL_TEMPLATE, urlDomain, this.operateProperties.getCloud().getOrganizationId());
        ResponseEntity responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity((MultiValueMap)headers), ClusterInfo.class, new Object[0]);
        ClusterInfo clusterInfo = (ClusterInfo)responseEntity.getBody();
        ClusterInfo.Permission operatePermissions = clusterInfo.getPermissions().getCluster().getOperate();
        if (operatePermissions.getRead() == false) throw new InsufficientAuthenticationException("User doesn't have read access");
        authentication.getPermissions().add(Permission.READ);
        if (operatePermissions.getDelete() == false) return;
        if (operatePermissions.getCreate() == false) return;
        if (operatePermissions.getUpdate() == false) return;
        authentication.getPermissions().add(Permission.WRITE);
    }

    private void sessionExpiresWhenAuthenticationExpires(HttpServletRequest req) {
        req.getSession().setMaxInactiveInterval(-1);
    }

    public String getAuthorizeUrl(HttpServletRequest req, HttpServletResponse res) {
        return this.authenticationController.buildAuthorizeUrl(req, res, this.getRedirectURI(req, "/sso-callback", true)).withAudience(this.operateProperties.getCloud().getPermissionAudience()).withScope(String.join((CharSequence)" ", SCOPES)).build();
    }

    public String getLogoutUrlFor(String returnTo) {
        return String.format(LOGOUT_URL_TEMPLATE, this.domain, this.clientId, returnTo);
    }

    public Tokens retrieveTokens(HttpServletRequest req, HttpServletResponse res) throws Exception {
        return (Tokens)RetryOperation.newBuilder().noOfRetry(10).delayInterval(500, TimeUnit.MILLISECONDS).retryOn(new Class[]{IdentityVerificationException.class}).retryConsumer(() -> this.authenticationController.handle(req, res)).build().retry();
    }

    public String getRedirectURI(HttpServletRequest req, String redirectTo) {
        return this.getRedirectURI(req, redirectTo, false);
    }

    public String getRedirectURI(HttpServletRequest req, String redirectTo, boolean omitContextPath) {
        String redirectUri = req.getScheme() + "://" + req.getServerName();
        if (req.getScheme().equals("http") && req.getServerPort() != 80 || req.getScheme().equals("https") && req.getServerPort() != 443) {
            redirectUri = redirectUri + ":" + req.getServerPort();
        }
        String clusterId = req.getContextPath().replace("/", "");
        if (!omitContextPath) return redirectUri + req.getContextPath() + redirectTo;
        return redirectUri + redirectTo + "?uuid=" + clusterId;
    }
}
