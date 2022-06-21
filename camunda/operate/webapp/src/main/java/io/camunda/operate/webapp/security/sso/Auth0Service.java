package io.camunda.operate.webapp.security.sso;

import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.Tokens;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.RetryOperation;
import io.camunda.operate.webapp.security.Permission;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Profile({"sso-auth"})
public class Auth0Service {
   private static final String LOGOUT_URL_TEMPLATE = "https://%s/v2/logout?client_id=%s&returnTo=%s";
   private static final String PERMISSION_URL_TEMPLATE = "%s/%s";
   private static final List SCOPES = List.of("openid", "profile", "email", "offline_access");
   @Autowired
   private BeanFactory beanFactory;
   @Autowired
   private AuthenticationController authenticationController;
   @Value("${camunda.operate.auth0.domain}")
   private String domain;
   @Value("${camunda.operate.auth0.backendDomain}")
   private String backendDomain;
   @Value("${camunda.operate.auth0.clientId}")
   private String clientId;
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   private RestTemplateBuilder builder;
   @Autowired
   @Qualifier("auth0_restTemplate")
   private RestTemplate restTemplate;

   @Bean({"auth0_restTemplate"})
   public RestTemplate restTemplate() {
      return this.builder.build();
   }

   public void authenticate(HttpServletRequest req, HttpServletResponse res) throws Auth0ServiceException {
      try {
         Tokens tokens = this.retrieveTokens(req, res);
         TokenAuthentication authentication = (TokenAuthentication)this.beanFactory.getBean(TokenAuthentication.class);
         this.checkPermission(authentication, tokens.getAccessToken());
         authentication.authenticate(tokens.getIdToken(), tokens.getRefreshToken());
         SecurityContextHolder.getContext().setAuthentication(authentication);
         this.sessionExpiresWhenAuthenticationExpires(req);
      } catch (Exception var5) {
         throw new Auth0ServiceException(var5);
      }
   }

   private void checkPermission(TokenAuthentication authentication, String accessToken) {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      String urlDomain = this.operateProperties.getCloud().getPermissionUrl();
      String url = String.format("%s/%s", urlDomain, this.operateProperties.getCloud().getOrganizationId());
      ResponseEntity responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(headers), ClusterInfo.class, new Object[0]);
      ClusterInfo clusterInfo = (ClusterInfo)responseEntity.getBody();
      ClusterInfo.Permission operatePermissions = clusterInfo.getPermissions().getCluster().getOperate();
      if (operatePermissions.getRead()) {
         authentication.getPermissions().add(Permission.READ);
         if (operatePermissions.getDelete() && operatePermissions.getCreate() && operatePermissions.getUpdate()) {
            authentication.getPermissions().add(Permission.WRITE);
         }

      } else {
         throw new InsufficientAuthenticationException("User doesn't have read access");
      }
   }

   private void sessionExpiresWhenAuthenticationExpires(HttpServletRequest req) {
      req.getSession().setMaxInactiveInterval(-1);
   }

   public String getAuthorizeUrl(HttpServletRequest req, HttpServletResponse res) {
      return this.authenticationController.buildAuthorizeUrl(req, res, this.getRedirectURI(req, "/sso-callback", true)).withAudience(this.operateProperties.getCloud().getPermissionAudience()).withScope(String.join(" ", SCOPES)).build();
   }

   public String getLogoutUrlFor(String returnTo) {
      return String.format("https://%s/v2/logout?client_id=%s&returnTo=%s", this.domain, this.clientId, returnTo);
   }

   public Tokens retrieveTokens(HttpServletRequest req, HttpServletResponse res) throws Exception {
      return (Tokens)RetryOperation.newBuilder().noOfRetry(10).delayInterval(500, TimeUnit.MILLISECONDS).retryOn(new Class[]{IdentityVerificationException.class}).retryConsumer(() -> {
         return this.authenticationController.handle(req, res);
      }).build().retry();
   }

   public String getRedirectURI(HttpServletRequest req, String redirectTo) {
      return this.getRedirectURI(req, redirectTo, false);
   }

   public String getRedirectURI(HttpServletRequest req, String redirectTo, boolean omitContextPath) {
      String var10000 = req.getScheme();
      String redirectUri = var10000 + "://" + req.getServerName();
      if (req.getScheme().equals("http") && req.getServerPort() != 80 || req.getScheme().equals("https") && req.getServerPort() != 443) {
         redirectUri = redirectUri + ":" + req.getServerPort();
      }

      String clusterId = req.getContextPath().replace("/", "");
      return omitContextPath ? redirectUri + redirectTo + "?uuid=" + clusterId : redirectUri + req.getContextPath() + redirectTo;
   }
}
