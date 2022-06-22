/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.security.Permission
 *  io.camunda.operate.webapp.security.UserService
 *  org.springframework.security.access.expression.SecurityExpressionRoot
 *  org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
 *  org.springframework.security.core.Authentication
 */
package io.camunda.operate.webapp.security;

import io.camunda.operate.webapp.security.Permission;
import io.camunda.operate.webapp.security.UserService;
import java.util.List;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomSecurityExpressionRoot
extends SecurityExpressionRoot
implements MethodSecurityExpressionOperations {
    private UserService<? extends Authentication> userService;
    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    public boolean hasPermission(String permission) {
        List permissions = this.userService.getCurrentUser().getPermissions();
        return permissions != null && permissions.contains(Permission.fromString((String)permission));
    }

    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    public Object getFilterObject() {
        return this.filterObject;
    }

    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    public Object getReturnObject() {
        return this.returnObject;
    }

    public Object getThis() {
        return this.target;
    }

    public void setThis(Object target) {
        this.target = target;
    }

    public CustomSecurityExpressionRoot setUserService(UserService<? extends Authentication> userService) {
        this.userService = userService;
        return this;
    }
}
