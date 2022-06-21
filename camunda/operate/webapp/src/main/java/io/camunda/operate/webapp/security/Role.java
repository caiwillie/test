package io.camunda.operate.webapp.security;

import java.util.Arrays;

public enum Role {
   OWNER,
   OPERATOR,
   USER;

   public static Role fromString(String roleAsString) {
      String roleName = roleAsString.replaceAll("\\s+", "_");
      Role[] var2 = values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Role role = var2[var4];
         if (role.name().equalsIgnoreCase(roleName)) {
            return role;
         }
      }

      throw new IllegalArgumentException(String.format("%s does not exists as Role in %s", roleAsString, Arrays.toString(values())));
   }
}
