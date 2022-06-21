package io.camunda.operate.webapp.security;

import java.util.List;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
   private UserService userService;
   private Object filterObject;
   private Object returnObject;
   private Object target;

   public CustomSecurityExpressionRoot(Authentication authentication) {
      super(authentication);
   }

   public boolean hasPermission(String permission) {
      List permissions = this.userService.getCurrentUser().getPermissions();
      return permissions != null && permissions.contains(Permission.fromString(permission));
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

   public CustomSecurityExpressionRoot setUserService(UserService userService) {
      this.userService = userService;
      return this;
   }
}
