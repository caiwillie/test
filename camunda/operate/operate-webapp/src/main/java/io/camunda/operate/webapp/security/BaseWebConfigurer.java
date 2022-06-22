/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.webapp.security.OperateProfileService
 *  io.camunda.operate.webapp.security.OperateURIs
 *  io.camunda.operate.webapp.security.oauth2.OAuth2WebConfigurer
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.http.entity.ContentType
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.http.HttpStatus
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
 *  org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer$AuthorizedUrl
 *  org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.AuthenticationException
 */
package io.camunda.operate.webapp.security;

import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.webapp.security.OperateProfileService;
import io.camunda.operate.webapp.security.OperateURIs;
import io.camunda.operate.webapp.security.oauth2.OAuth2WebConfigurer;
import java.io.IOException;
import java.io.PrintWriter;
import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public abstract class BaseWebConfigurer
extends WebSecurityConfigurerAdapter {
    protected final Logger logger = LoggerFactory.getLogger(((Object)((Object)this)).getClass());
    @Autowired
    protected OperateProperties operateProperties;
    @Autowired
    OperateProfileService errorMessageService;
    @Autowired
    private OAuth2WebConfigurer oAuth2WebConfigurer;

    protected void configure(HttpSecurity http) throws Exception {
        ((HttpSecurity)((HttpSecurity)((FormLoginConfigurer)((FormLoginConfigurer)((FormLoginConfigurer)((FormLoginConfigurer)((HttpSecurity)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)http.csrf().disable()).authorizeRequests().antMatchers(OperateURIs.AUTH_WHITELIST)).permitAll().antMatchers(new String[]{"/api/**", "/v*/**"})).authenticated().and()).formLogin().loginProcessingUrl("/api/login")).successHandler(this::successHandler)).failureHandler(this::failureHandler)).permitAll()).and()).logout().logoutUrl("/api/logout").logoutSuccessHandler(this::logoutSuccessHandler).permitAll().deleteCookies(new String[]{"OPERATE-SESSION"}).clearAuthentication(true).invalidateHttpSession(true).and()).exceptionHandling().authenticationEntryPoint(this::failureHandler);
        this.oAuth2WebConfigurer.configure(http);
    }

    protected void logoutSuccessHandler(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    protected void failureHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        String requestedUrl = request.getRequestURI();
        if (requestedUrl.contains("api")) {
            this.sendError(request, response, ex);
        } else {
            this.storeRequestedUrlAndRedirectToLogin(request, response, requestedUrl);
        }
    }

    private void storeRequestedUrlAndRedirectToLogin(HttpServletRequest request, HttpServletResponse response, String requestedUrl) throws IOException {
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            requestedUrl = (String)requestedUrl + "?" + request.getQueryString();
        }
        this.logger.debug("Try to access protected resource {}. Save it for later redirect", requestedUrl);
        request.getSession(true).setAttribute("requestedUrl", requestedUrl);
        response.sendRedirect(request.getContextPath() + "/api/login");
    }

    private void successHandler(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    protected void sendError(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        request.getSession().invalidate();
        response.reset();
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        String jsonResponse = Json.createObjectBuilder().add("message", this.errorMessageService.getMessageByProfileFor((Exception)ex)).build().toString();
        writer.append(jsonResponse);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
}
