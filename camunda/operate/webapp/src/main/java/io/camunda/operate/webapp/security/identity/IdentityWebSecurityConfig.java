package io.camunda.operate.webapp.security.identity;

import io.camunda.identity.sdk.Identity;
import io.camunda.identity.sdk.IdentityConfiguration;
import io.camunda.operate.property.IdentityProperties;
import io.camunda.operate.webapp.security.BaseWebConfigurer;
import io.camunda.operate.webapp.security.OperateURIs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.stereotype.Component;

@Profile({"identity-auth"})
@Configuration
@EnableWebSecurity
@Component("webSecurityConfig")
public class IdentityWebSecurityConfig extends BaseWebConfigurer {
   @Bean
   public Identity identity() throws IllegalArgumentException {
      IdentityProperties props = this.operateProperties.getIdentity();
      IdentityConfiguration configuration = new IdentityConfiguration(props.getIssuerUrl(), props.getIssuerBackendUrl(), props.getClientId(), props.getClientSecret(), props.getAudience());
      return new Identity(configuration);
   }

   protected void configure(HttpSecurity http) throws Exception {
      ((HttpSecurity)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)http.csrf().disable()).authorizeRequests().antMatchers(OperateURIs.AUTH_WHITELIST)).permitAll().antMatchers(new String[]{"/api/**", "/"})).authenticated().and()).exceptionHandling().authenticationEntryPoint(this::failureHandler);
   }
}
