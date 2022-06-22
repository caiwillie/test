/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.security.BaseWebConfigurer
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Profile
 *  org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
 *  org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 *  org.springframework.security.core.userdetails.UserDetailsService
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security;

import io.camunda.operate.webapp.security.BaseWebConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Profile(value={"auth"})
@EnableWebSecurity
@Configuration
@Component(value="webSecurityConfig")
public class WebSecurityConfig
extends BaseWebConfigurer {
    @Autowired
    private UserDetailsService userDetailsService;

    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(this.userDetailsService);
    }
}
