package io.camunda.operate.webapp.security.sso;

import com.auth0.AuthenticationController;
import io.camunda.operate.webapp.security.BaseWebConfigurer;
import io.camunda.operate.webapp.security.OperateURIs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.stereotype.Component;

@Profile({"sso-auth"})
@Configuration
@EnableWebSecurity
@Component("webSecurityConfig")
public class SSOWebSecurityConfig extends BaseWebConfigurer {
   @Bean
   public AuthenticationController authenticationController() {
      return AuthenticationController.newBuilder(this.operateProperties.getAuth0().getDomain(), this.operateProperties.getAuth0().getClientId(), this.operateProperties.getAuth0().getClientSecret()).build();
   }

   protected void configure(HttpSecurity http) throws Exception {
      ((HttpSecurity)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)http.csrf().disable()).authorizeRequests().antMatchers(OperateURIs.AUTH_WHITELIST)).permitAll().antMatchers(new String[]{"/api/**", "/"})).authenticated().and()).exceptionHandling().authenticationEntryPoint(this::failureHandler);
   }
}
