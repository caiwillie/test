/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.webapp.rest.dto.UserDto
 *  io.camunda.operate.webapp.rest.exception.UserNotFoundException
 *  io.camunda.operate.webapp.security.UserService
 *  io.camunda.operate.webapp.security.ldap.LDAPUserService$LdapUserAttributesMapper
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.Profile
 *  org.springframework.ldap.core.AttributesMapper
 *  org.springframework.ldap.core.LdapTemplate
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.ldap.userdetails.LdapUserDetails
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security.ldap;

import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.webapp.rest.dto.UserDto;
import io.camunda.operate.webapp.rest.exception.UserNotFoundException;
import io.camunda.operate.webapp.security.Permission;
import io.camunda.operate.webapp.security.UserService;
import io.camunda.operate.webapp.security.ldap.LDAPUserService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

@Component
@Profile(value={"ldap-auth"})
public class LDAPUserService
implements UserService<Authentication> {
    private static final Logger logger = LoggerFactory.getLogger(LDAPUserService.class);
    @Autowired
    private LdapTemplate ldapTemplate;
    @Autowired
    private OperateProperties operateProperties;
    private Map<String, UserDto> ldapDnToUser = new ConcurrentHashMap<String, UserDto>();

    public UserDto createUserDtoFrom(Authentication authentication) {
        LdapUserDetails userDetails = (LdapUserDetails)authentication.getPrincipal();
        String dn = userDetails.getDn();
        if (this.ldapDnToUser.containsKey(dn)) return this.ldapDnToUser.get(dn);
        logger.info(String.format("Do a LDAP Lookup for user DN: %s)", dn));
        try {
            this.ldapDnToUser.put(dn, (UserDto)this.ldapTemplate.lookup(dn, (AttributesMapper)new LdapUserAttributesMapper()));
        }
        catch (Exception ex) {
            throw new UserNotFoundException(String.format("Couldn't find user for dn %s", dn));
        }
        return this.ldapDnToUser.get(dn);
    }

    public void cleanUp(Authentication authentication) {
        LdapUserDetails userDetails = (LdapUserDetails)authentication.getPrincipal();
        String dn = userDetails.getDn();
        this.ldapDnToUser.remove(dn);
    }

    private class LdapUserAttributesMapper implements AttributesMapper<UserDto> {
        private LdapUserAttributesMapper() {
        }

        public UserDto mapFromAttributes(Attributes attrs) throws NamingException {
            Attribute displayNameAttr;
            UserDto userDto = new UserDto().setCanLogout(true);
            Attribute userIdAttr = attrs.get(LDAPUserService.this.operateProperties.getLdap().getUserIdAttrName());
            if (userIdAttr != null) {
                userDto.setUserId((String)userIdAttr.get());
            }
            if ((displayNameAttr = attrs.get(LDAPUserService.this.operateProperties.getLdap().getDisplayNameAttrName())) != null) {
                userDto.setDisplayName((String)displayNameAttr.get());
            }
            userDto.setPermissions(List.of(Permission.READ, Permission.WRITE));
            return userDto;
        }
    }
}
