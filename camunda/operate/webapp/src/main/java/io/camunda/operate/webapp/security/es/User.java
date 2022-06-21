package io.camunda.operate.webapp.security.es;

import io.camunda.operate.util.CollectionUtil;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class User extends org.springframework.security.core.userdetails.User {
   private String userId;
   private String displayName;
   private List roles;
   private boolean canLogout = true;

   private static Collection toAuthorities(List roles) {
      return CollectionUtil.map(roles, (role) -> {
         return new SimpleGrantedAuthority("ROLE_" + role);
      });
   }

   public User(String userId, String displayName, String password, List roles) {
      super(userId, password, toAuthorities(roles));
      this.userId = userId;
      this.displayName = displayName;
      this.roles = roles;
   }

   public String getUserId() {
      return this.userId;
   }

   public User setUserId(String userId) {
      this.userId = userId;
      return this;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public User setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
   }

   public boolean isCanLogout() {
      return this.canLogout;
   }

   public User setCanLogout(boolean canLogout) {
      this.canLogout = canLogout;
      return this;
   }

   public User setRoles(List roles) {
      this.roles = roles;
      return this;
   }

   public List getRoles() {
      return this.roles;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            User user = (User)o;
            return this.canLogout == user.canLogout && this.userId.equals(user.userId) && this.displayName.equals(user.displayName) && this.roles.equals(user.roles);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.userId, this.displayName, this.roles, this.canLogout});
   }
}
