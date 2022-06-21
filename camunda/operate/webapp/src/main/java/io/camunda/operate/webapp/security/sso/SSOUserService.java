package io.camunda.operate.webapp.security.sso;

import com.auth0.jwt.interfaces.Claim;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.webapp.rest.dto.UserDto;
import io.camunda.operate.webapp.security.UserService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"sso-auth"})
public class SSOUserService implements UserService {
   @Autowired
   private OperateProperties operateProperties;

   public UserDto createUserDtoFrom(TokenAuthentication authentication) {
      Map claims = authentication.getClaims();
      String name = "No name";
      if (claims.containsKey(this.operateProperties.getAuth0().getNameKey())) {
         name = ((Claim)claims.get(this.operateProperties.getAuth0().getNameKey())).asString();
      }

      return (new UserDto()).setUserId(authentication.getName()).setDisplayName(name).setCanLogout(false).setPermissions(authentication.getPermissions());
   }
}
