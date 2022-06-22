/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.UserDto
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.context.SecurityContext
 *  org.springframework.security.core.context.SecurityContextHolder
 */
package io.camunda.operate.webapp.security;

import io.camunda.operate.webapp.rest.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public interface UserService<T extends Authentication> {
    default public UserDto getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        return this.createUserDtoFrom((T)context.getAuthentication());
    }

    public UserDto createUserDtoFrom(T var1);
}
