/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.UserDto
 *  io.camunda.operate.webapp.security.UserService
 *  io.camunda.operate.webapp.security.identity.IdentityAuthentication
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security.identity;

import io.camunda.operate.webapp.rest.dto.UserDto;
import io.camunda.operate.webapp.security.UserService;
import io.camunda.operate.webapp.security.identity.IdentityAuthentication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value={"identity-auth"})
public class IdentityUserService
implements UserService<IdentityAuthentication> {
    public UserDto createUserDtoFrom(IdentityAuthentication authentication) {
        return new UserDto().setUserId(authentication.getId()).setDisplayName(authentication.getName()).setCanLogout(true).setPermissions(authentication.getPermissions());
    }
}
