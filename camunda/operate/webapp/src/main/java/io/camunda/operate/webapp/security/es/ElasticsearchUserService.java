/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.UserDto
 *  io.camunda.operate.webapp.security.RolePermissionService
 *  io.camunda.operate.webapp.security.UserService
 *  io.camunda.operate.webapp.security.es.User
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.Profile
 *  org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security.es;

import io.camunda.operate.webapp.rest.dto.UserDto;
import io.camunda.operate.webapp.security.RolePermissionService;
import io.camunda.operate.webapp.security.UserService;
import io.camunda.operate.webapp.security.es.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Profile(value={"!ldap-auth & ! sso-auth & !identity-auth"})
public class ElasticsearchUserService
implements UserService<UsernamePasswordAuthenticationToken> {
    @Autowired
    private RolePermissionService rolePermissionService;

    public UserDto createUserDtoFrom(UsernamePasswordAuthenticationToken authentication) {
        User user = (User)authentication.getPrincipal();
        return new UserDto().setUserId(user.getUserId()).setDisplayName(user.getDisplayName()).setCanLogout(true).setPermissions(this.rolePermissionService.getPermissions(user.getRoles()));
    }
}
