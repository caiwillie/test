/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.UserDto
 *  io.camunda.operate.webapp.rest.exception.UserNotFoundException
 *  io.camunda.operate.webapp.security.UserService
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.security.core.userdetails.UsernameNotFoundException
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package io.camunda.operate.webapp.rest;

import io.camunda.operate.webapp.rest.dto.UserDto;
import io.camunda.operate.webapp.rest.exception.UserNotFoundException;
import io.camunda.operate.webapp.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/authentications"})
public class AuthenticationRestService {
    public static final String AUTHENTICATION_URL = "/api/authentications";
    public static final String USER_ENDPOINT = "/user";
    @Autowired
    private UserService userService;

    @GetMapping(path={"/user"})
    public UserDto getCurrentAuthentication() {
        try {
            return this.userService.getCurrentUser();
        }
        catch (UsernameNotFoundException e) {
            throw new UserNotFoundException("Current user couldn't be found", (Throwable)e);
        }
    }
}
