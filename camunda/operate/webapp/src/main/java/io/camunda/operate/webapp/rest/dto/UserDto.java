package io.camunda.operate.webapp.rest.dto;

import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;

public class UserDto {
   private String userId;
   private String displayName;
   private boolean canLogout;
   private List permissions;

   public boolean isCanLogout() {
      return this.canLogout;
   }

   public UserDto setCanLogout(boolean canLogout) {
      this.canLogout = canLogout;
      return this;
   }

   public String getUserId() {
      return this.userId;
   }

   public UserDto setUserId(String userId) {
      this.userId = userId;
      return this;
   }

   public String getDisplayName() {
      return !StringUtils.hasText(this.displayName) ? this.userId : this.displayName;
   }

   public UserDto setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
   }

   public String getUsername() {
      return this.getDisplayName();
   }

   public List getPermissions() {
      return this.permissions;
   }

   public UserDto setPermissions(List permissions) {
      this.permissions = permissions;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         UserDto userDto = (UserDto)o;
         return this.canLogout == userDto.canLogout && this.userId.equals(userDto.userId) && this.displayName.equals(userDto.displayName) && this.permissions.equals(userDto.permissions);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.userId, this.displayName, this.canLogout, this.permissions});
   }
}
