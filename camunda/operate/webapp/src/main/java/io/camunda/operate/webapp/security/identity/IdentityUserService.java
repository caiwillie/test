package io.camunda.operate.webapp.security.identity;

import io.camunda.operate.webapp.rest.dto.UserDto;
import io.camunda.operate.webapp.security.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"identity-auth"})
public class IdentityUserService implements UserService {
   public UserDto createUserDtoFrom(IdentityAuthentication authentication) {
      return (new UserDto()).setUserId(authentication.getId()).setDisplayName(authentication.getName()).setCanLogout(true).setPermissions(authentication.getPermissions());
   }
}
