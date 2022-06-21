package io.camunda.operate.webapp.security;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionService {
   private final Map roles2permissions = new EnumMap(Role.class);

   @PostConstruct
   public void init() {
      this.roles2permissions.put(Role.USER, List.of(Permission.READ));
      this.roles2permissions.put(Role.OPERATOR, List.of(Permission.READ, Permission.WRITE));
      this.roles2permissions.put(Role.OWNER, List.of(Permission.READ, Permission.WRITE));
   }

   public List getPermissions(List roles) {
      return (List)roles.stream().map(this::getPermissionsForRole).flatMap(Collection::stream).collect(Collectors.toList());
   }

   private List getPermissionsForRole(Role role) {
      return (List)this.roles2permissions.get(role);
   }
}
