package io.camunda.operate.entities;

import java.util.List;
import java.util.Objects;

public class UserEntity extends OperateEntity {
   private String userId;
   private String displayName;
   private String password;
   private List roles;

   public List getRoles() {
      return this.roles;
   }

   public UserEntity setRoles(List roles) {
      this.roles = roles;
      return this;
   }

   public String getPassword() {
      return this.password;
   }

   public UserEntity setPassword(String password) {
      this.password = password;
      return this;
   }

   public String getUserId() {
      return this.userId;
   }

   public UserEntity setUserId(String userId) {
      this.userId = userId;
      return this;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public UserEntity setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            UserEntity that = (UserEntity)o;
            return this.userId.equals(that.userId) && this.displayName.equals(that.displayName) && this.password.equals(that.password) && this.roles.equals(that.roles);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.userId, this.displayName, this.password, this.roles});
   }
}
