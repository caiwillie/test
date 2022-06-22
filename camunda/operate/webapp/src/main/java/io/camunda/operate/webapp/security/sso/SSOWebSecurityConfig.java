/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.auth0.AuthenticationController
 *  io.camunda.operate.webapp.security.BaseWebConfigurer
 *  io.camunda.operate.webapp.security.OperateURIs
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Profile
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 *  org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer$AuthorizedUrl
 *  org.springframework.stereotype.Component
 */
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

@Profile(value={"sso-auth"})
@Configuration
@EnableWebSecurity
@Component(value="webSecurityConfig")
public class SSOWebSecurityConfig
extends BaseWebConfigurer {
    @Bean
    public AuthenticationController authenticationController() {
        return AuthenticationController.newBuilder((String)this.operateProperties.getAuth0().getDomain(), (String)this.operateProperties.getAuth0().getClientId(), (String)this.operateProperties.getAuth0().getClientSecret()).build();
    }

    protected void configure(HttpSecurity http) throws Exception {
        ((HttpSecurity)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)http.csrf().disable()).authorizeRequests().antMatchers(OperateURIs.AUTH_WHITELIST)).permitAll().antMatchers(new String[]{"/api/**", "/"})).authenticated().and()).exceptionHandling().authenticationEntryPoint((arg_0, arg_1, arg_2) -> ((SSOWebSecurityConfig)this).failureHandler(arg_0, arg_1, arg_2));
    }
}
