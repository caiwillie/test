package io.camunda.operate.webapp.security;

import io.camunda.operate.webapp.rest.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public interface UserService {
   default UserDto getCurrentUser() {
      SecurityContext context = SecurityContextHolder.getContext();
      return this.createUserDtoFrom(context.getAuthentication());
   }

   UserDto createUserDtoFrom(Authentication var1);
}
