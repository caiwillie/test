package io.camunda.operate.webapp.security;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
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

   private UserService getUserService() {
      return (UserService)this.beanFactory.getBean(UserService.class);
   }
}
