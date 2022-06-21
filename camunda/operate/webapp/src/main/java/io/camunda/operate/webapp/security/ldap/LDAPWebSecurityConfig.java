package io.camunda.operate.webapp.security.ldap;

import io.camunda.operate.property.LdapProperties;
import io.camunda.operate.webapp.security.BaseWebConfigurer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Profile({"ldap-auth"})
@Configuration
@EnableWebSecurity
@Component("webSecurityConfig")
public class LDAPWebSecurityConfig extends BaseWebConfigurer {
   @Autowired
   private LdapContextSource contextSource;
   @Autowired
   private LDAPUserService userService;

   public void configure(AuthenticationManagerBuilder auth) throws Exception {
      LdapProperties ldapConfig = this.operateProperties.getLdap();
      if (StringUtils.hasText(ldapConfig.getDomain())) {
         this.setUpActiveDirectoryLDAP(auth, ldapConfig);
      } else {
         this.setupStandardLDAP(auth, ldapConfig);
      }

   }

   private void setUpActiveDirectoryLDAP(AuthenticationManagerBuilder auth, LdapProperties ldapConfig) {
      ActiveDirectoryLdapAuthenticationProvider adLDAPProvider = new ActiveDirectoryLdapAuthenticationProvider(ldapConfig.getDomain(), ldapConfig.getUrl(), ldapConfig.getBaseDn());
      if (StringUtils.hasText(ldapConfig.getUserSearchFilter())) {
         adLDAPProvider.setSearchFilter(ldapConfig.getUserSearchFilter());
      }

      adLDAPProvider.setConvertSubErrorCodesToExceptions(true);
      auth.authenticationProvider(adLDAPProvider);
   }

   private void setupStandardLDAP(AuthenticationManagerBuilder auth, LdapProperties ldapConfig) throws Exception {
      LdapAuthenticationProviderConfigurer.ContextSourceBuilder var10000 = auth.ldapAuthentication().userDnPatterns(new String[]{ldapConfig.getUserDnPatterns()}).userSearchFilter(ldapConfig.getUserSearchFilter()).userSearchBase(ldapConfig.getUserSearchBase()).contextSource();
      String var10001 = ldapConfig.getUrl();
      var10000.url(var10001 + ldapConfig.getBaseDn()).managerDn(ldapConfig.getManagerDn()).managerPassword(ldapConfig.getManagerPassword());
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
      } catch (Exception var3) {
         this.logger.error("Authentication for lookup failed.", var3);
      }

   }

   @Bean
   public LdapTemplate ldapTemplate() {
      this.authenticateContextSource(this.contextSource);
      return new LdapTemplate(this.contextSource);
   }

   protected void logoutSuccessHandler(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
      this.userService.cleanUp(authentication);
      super.logoutSuccessHandler(request, response, authentication);
   }
}
