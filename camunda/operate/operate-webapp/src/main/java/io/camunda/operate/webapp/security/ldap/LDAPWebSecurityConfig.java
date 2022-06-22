/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.property.LdapProperties
 *  io.camunda.operate.webapp.security.BaseWebConfigurer
 *  io.camunda.operate.webapp.security.ldap.LDAPUserService
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Profile
 *  org.springframework.ldap.core.ContextSource
 *  org.springframework.ldap.core.LdapTemplate
 *  org.springframework.ldap.core.support.LdapContextSource
 *  org.springframework.security.authentication.AuthenticationProvider
 *  org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
 *  org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider
 *  org.springframework.stereotype.Component
 *  org.springframework.util.StringUtils
 */
package io.camunda.operate.webapp.security.ldap;

import io.camunda.operate.property.LdapProperties;
import io.camunda.operate.webapp.security.BaseWebConfigurer;
import io.camunda.operate.webapp.security.ldap.LDAPUserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Profile(value={"ldap-auth"})
@Configuration
@EnableWebSecurity
@Component(value="webSecurityConfig")
public class LDAPWebSecurityConfig
extends BaseWebConfigurer {
    @Autowired
    private LdapContextSource contextSource;
    @Autowired
    private LDAPUserService userService;

    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        LdapProperties ldapConfig = this.operateProperties.getLdap();
        if (StringUtils.hasText((String)ldapConfig.getDomain())) {
            this.setUpActiveDirectoryLDAP(auth, ldapConfig);
        } else {
            this.setupStandardLDAP(auth, ldapConfig);
        }
    }

    private void setUpActiveDirectoryLDAP(AuthenticationManagerBuilder auth, LdapProperties ldapConfig) {
        ActiveDirectoryLdapAuthenticationProvider adLDAPProvider = new ActiveDirectoryLdapAuthenticationProvider(ldapConfig.getDomain(), ldapConfig.getUrl(), ldapConfig.getBaseDn());
        if (StringUtils.hasText((String)ldapConfig.getUserSearchFilter())) {
            adLDAPProvider.setSearchFilter(ldapConfig.getUserSearchFilter());
        }
        adLDAPProvider.setConvertSubErrorCodesToExceptions(true);
        auth.authenticationProvider((AuthenticationProvider)adLDAPProvider);
    }

    private void setupStandardLDAP(AuthenticationManagerBuilder auth, LdapProperties ldapConfig) throws Exception {
        auth.ldapAuthentication().userDnPatterns(new String[]{ldapConfig.getUserDnPatterns()}).userSearchFilter(ldapConfig.getUserSearchFilter()).userSearchBase(ldapConfig.getUserSearchBase()).contextSource().url(ldapConfig.getUrl() + ldapConfig.getBaseDn()).managerDn(ldapConfig.getManagerDn()).managerPassword(ldapConfig.getManagerPassword());
    }

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(this.operateProperties.getLdap().getUrl());
        contextSource.setUserDn(this.operateProperties.getLdap().getManagerDn());
        contextSource.setPassword(this.operateProperties.getLdap().getManagerPassword());
        return contextSource;
    }

    private void authenticateContextSource(LdapContextSource contextSource) {
        try {
            contextSource.getContext(this.operateProperties.getLdap().getManagerDn(), this.operateProperties.getLdap().getManagerPassword());
        }
        catch (Exception e) {
            this.logger.error("Authentication for lookup failed.", e);
        }
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        this.authenticateContextSource(this.contextSource);
        return new LdapTemplate((ContextSource)this.contextSource);
    }

    protected void logoutSuccessHandler(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        this.userService.cleanUp(authentication);
        super.logoutSuccessHandler(request, response, authentication);
    }
}
