/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.security.CustomSecurityExpressionRoot
 *  io.camunda.operate.webapp.security.UserService
 *  org.aopalliance.intercept.MethodInvocation
 *  org.springframework.beans.factory.BeanFactory
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
 *  org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
 *  org.springframework.security.core.Authentication
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security;

import io.camunda.operate.webapp.security.CustomSecurityExpressionRoot;
import io.camunda.operate.webapp.security.UserService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomMethodSecurityExpressionHandler
extends DefaultMethodSecurityExpressionHandler {
    @Autowired
    BeanFactory beanFactory;

    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
        CustomSecurityExpressionRoot root = new CustomSecurityExpressionRoot(authentication);
        root.setUserService(this.getUserService());
        root.setThis(invocation.getThis());
        root.setPermissionEvaluator(this.getPermissionEvaluator());
        root.setTrustResolver(this.getTrustResolver());
        root.setRoleHierarchy(this.getRoleHierarchy());
        root.setDefaultRolePrefix(this.getDefaultRolePrefix());
        return root;
    }

    private UserService<? extends Authentication> getUserService() {
        return (UserService)this.beanFactory.getBean(UserService.class);
    }
}
