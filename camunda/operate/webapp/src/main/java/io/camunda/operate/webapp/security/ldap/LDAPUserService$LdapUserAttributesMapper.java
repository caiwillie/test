/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.UserDto
 *  io.camunda.operate.webapp.security.Permission
 *  org.springframework.ldap.core.AttributesMapper
 */
package io.camunda.operate.webapp.security.ldap;

import io.camunda.operate.webapp.rest.dto.UserDto;
import io.camunda.operate.webapp.security.Permission;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.springframework.ldap.core.AttributesMapper;
/*

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
*/
