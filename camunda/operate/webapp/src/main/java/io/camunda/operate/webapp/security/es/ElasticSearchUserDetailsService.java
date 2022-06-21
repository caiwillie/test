package io.camunda.operate.webapp.security.es;

import io.camunda.operate.entities.UserEntity;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.operate.webapp.security.Role;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile({"!ldap-auth & !sso-auth & !identity-auth"})
public class ElasticSearchUserDetailsService implements UserDetailsService {
   private static final Logger logger = LoggerFactory.getLogger(ElasticSearchUserDetailsService.class);
   private static final String ACT_USERNAME = "act";
   private static final String ACT_PASSWORD = "act";
   private static final String READ_ONLY_USER = "view";
   @Autowired
   private UserStorage userStorage;
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   private PasswordEncoder passwordEncoder;

   @Bean
   public PasswordEncoder getPasswordEncoder() {
      return new BCryptPasswordEncoder();
   }

   public void initializeUsers() {
      if (this.operateProperties.getElasticsearch().isCreateSchema()) {
         String userId = this.operateProperties.getUserId();
         if (!this.userExists(userId)) {
            this.addUserWith(userId, this.operateProperties.getDisplayName(), this.operateProperties.getPassword(), this.operateProperties.getRoles());
         }

         if (!this.userExists("view")) {
            this.addUserWith("view", "view", "view", List.of(Role.USER.name()));
         }

         if (!this.userExists("act")) {
            this.addUserWith("act", "act", "act", List.of(Role.OPERATOR.name()));
         }
      }

   }

   private ElasticSearchUserDetailsService addUserWith(String userId, String displayName, String password, List roles) {
      logger.info("Create user in ElasticSearch for userId {}", userId);
      String passwordEncoded = this.passwordEncoder.encode(password);
      UserEntity userEntity = ((UserEntity)(new UserEntity()).setId(userId)).setUserId(userId).setDisplayName(displayName).setPassword(passwordEncoded).setRoles(roles);
      this.userStorage.create(userEntity);
      return this;
   }

   public User loadUserByUsername(String userId) {
      try {
         UserEntity userEntity = this.userStorage.getByUserId(userId);
         return new User(userEntity.getUserId(), userEntity.getDisplayName(), userEntity.getPassword(), CollectionUtil.map(userEntity.getRoles(), Role::fromString));
      } catch (NotFoundException var3) {
         throw new UsernameNotFoundException(String.format("User with userId '%s' not found.", userId), var3);
      }
   }

   private boolean userExists(String userId) {
      try {
         return this.userStorage.getByUserId(userId) != null;
      } catch (Exception var3) {
         return false;
      }
   }
}
