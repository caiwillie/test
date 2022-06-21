package io.camunda.operate.schema.indices;

import org.springframework.stereotype.Component;

@Component
public class UserIndex extends AbstractIndexDescriptor {
   public static final String INDEX_NAME = "user";
   public static final String ID = "id";
   public static final String USER_ID = "userId";
   public static final String PASSWORD = "password";
   public static final String ROLES = "roles";
   public static final String DISPLAY_NAME = "displayName";

   public String getIndexName() {
      return "user";
   }

   public String getVersion() {
      return "1.2.0";
   }
}
