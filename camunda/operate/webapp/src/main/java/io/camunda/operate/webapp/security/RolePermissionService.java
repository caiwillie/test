/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.security.Permission
 *  io.camunda.operate.webapp.security.Role
 *  javax.annotation.PostConstruct
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security;

import io.camunda.operate.webapp.security.Permission;
import io.camunda.operate.webapp.security.Role;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionService {
    private final Map<Role, List<Permission>> roles2permissions = new EnumMap<Role, List<Permission>>(Role.class);

    @PostConstruct
    public void init() {
        this.roles2permissions.put(Role.USER, List.of(Permission.READ));
        this.roles2permissions.put(Role.OPERATOR, List.of(Permission.READ, Permission.WRITE));
        this.roles2permissions.put(Role.OWNER, List.of(Permission.READ, Permission.WRITE));
    }

    public List<Permission> getPermissions(List<Role> roles) {
        return roles.stream().map(this::getPermissionsForRole).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<Permission> getPermissionsForRole(Role role) {
        return this.roles2permissions.get(role);
    }
}
