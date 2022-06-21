package io.camunda.operate.webapp.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

public enum Permission {
   @JsonProperty("read")
   READ,
   @JsonProperty("write")
   WRITE;

   public static Permission fromString(String permissionAsString) {
      Permission[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Permission permission = var1[var3];
         if (permission.name().equalsIgnoreCase(permissionAsString)) {
            return permission;
         }
      }

      throw new IllegalArgumentException(String.format("%s does not exists as Permission in %s", permissionAsString, Arrays.toString(values())));
   }
}
