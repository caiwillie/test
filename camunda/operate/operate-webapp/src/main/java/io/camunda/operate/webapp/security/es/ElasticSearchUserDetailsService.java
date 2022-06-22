/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.UserEntity
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  io.camunda.operate.webapp.security.Role
 *  io.camunda.operate.webapp.security.es.User
 *  io.camunda.operate.webapp.security.es.UserStorage
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Profile
 *  org.springframework.security.core.userdetails.UserDetailsService
 *  org.springframework.security.core.userdetails.UsernameNotFoundException
 *  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 *  org.springframework.security.crypto.password.PasswordEncoder
 */
package io.camunda.operate.webapp.security.es;

import io.camunda.operate.entities.UserEntity;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import io.camunda.operate.webapp.security.Role;
import io.camunda.operate.webapp.security.es.User;
import io.camunda.operate.webapp.security.es.UserStorage;
import java.util.Collection;
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
@Profile(value={"!ldap-auth & !sso-auth & !identity-auth"})
public class ElasticSearchUserDetailsService
implements UserDetailsService {
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
        if (!this.operateProperties.getElasticsearch().isCreateSchema()) return;
        String userId = this.operateProperties.getUserId();
        if (!this.userExists(userId)) {
            this.addUserWith(userId, this.operateProperties.getDisplayName(), this.operateProperties.getPassword(), this.operateProperties.getRoles());
        }
        if (!this.userExists(READ_ONLY_USER)) {
            this.addUserWith(READ_ONLY_USER, READ_ONLY_USER, READ_ONLY_USER, List.of(Role.USER.name()));
        }
        if (this.userExists("act")) return;
        this.addUserWith("act", "act", "act", List.of(Role.OPERATOR.name()));
    }

    private ElasticSearchUserDetailsService addUserWith(String userId, String displayName, String password, List<String> roles) {
        logger.info("Create user in ElasticSearch for userId {}", (Object)userId);
        String passwordEncoded = this.passwordEncoder.encode((CharSequence)password);
        UserEntity userEntity = ((UserEntity)new UserEntity().setId(userId)).setUserId(userId).setDisplayName(displayName).setPassword(passwordEncoded).setRoles(roles);
        this.userStorage.create(userEntity);
        return this;
    }

    public User loadUserByUsername(String userId) {
        try {
            UserEntity userEntity = this.userStorage.getByUserId(userId);
            return new User(userEntity.getUserId(), userEntity.getDisplayName(), userEntity.getPassword(), CollectionUtil.map((Collection)userEntity.getRoles(), Role::fromString));
        }
        catch (NotFoundException e) {
            throw new UsernameNotFoundException(String.format("User with userId '%s' not found.", userId), (Throwable)e);
        }
    }

    private boolean userExists(String userId) {
        try {
            return this.userStorage.getByUserId(userId) != null;
        }
        catch (Exception t) {
            return false;
        }
    }
}
