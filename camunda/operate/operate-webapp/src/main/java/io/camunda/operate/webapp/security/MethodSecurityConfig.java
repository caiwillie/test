/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.security.CustomMethodSecurityExpressionHandler
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
 *  org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
 *  org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
 */
package io.camunda.operate.webapp.security;

import io.camunda.operate.webapp.security.CustomMethodSecurityExpressionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class MethodSecurityConfig
extends GlobalMethodSecurityConfiguration {
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new CustomMethodSecurityExpressionHandler();
    }
}
