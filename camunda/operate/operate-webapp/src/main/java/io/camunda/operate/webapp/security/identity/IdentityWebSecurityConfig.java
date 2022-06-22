/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.identity.sdk.Identity
 *  io.camunda.identity.sdk.IdentityConfiguration
 *  io.camunda.operate.property.IdentityProperties
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

@Profile(value={"identity-auth"})
@Configuration
@EnableWebSecurity
@Component(value="webSecurityConfig")
public class IdentityWebSecurityConfig
extends BaseWebConfigurer {
    @Bean
    public Identity identity() throws IllegalArgumentException {
        IdentityProperties props = this.operateProperties.getIdentity();
        IdentityConfiguration configuration = new IdentityConfiguration(props.getIssuerUrl(), props.getIssuerBackendUrl(), props.getClientId(), props.getClientSecret(), props.getAudience());
        return new Identity(configuration);
    }

    protected void configure(HttpSecurity http) throws Exception {
        ((HttpSecurity)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)http.csrf().disable()).authorizeRequests().antMatchers(OperateURIs.AUTH_WHITELIST)).permitAll().antMatchers(new String[]{"/api/**", "/"})).authenticated().and()).exceptionHandling().authenticationEntryPoint((arg_0, arg_1, arg_2) -> ((IdentityWebSecurityConfig)this).failureHandler(arg_0, arg_1, arg_2));
    }
}
