/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.security.Permission
 *  org.springframework.util.StringUtils
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.webapp.security.Permission;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;

public class UserDto {
    private String userId;
    private String displayName;
    private boolean canLogout;
    private List<Permission> permissions;

    public boolean isCanLogout() {
        return this.canLogout;
    }

    public UserDto setCanLogout(boolean canLogout) {
        this.canLogout = canLogout;
        return this;
    }

    public String getUserId() {
        return this.userId;
    }

    public UserDto setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getDisplayName() {
        if (StringUtils.hasText((String)this.displayName)) return this.displayName;
        return this.userId;
    }

    public UserDto setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getUsername() {
        return this.getDisplayName();
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }

    public UserDto setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto)o;
        return this.canLogout == userDto.canLogout && this.userId.equals(userDto.userId) && this.displayName.equals(userDto.displayName) && this.permissions.equals(userDto.permissions);
    }

    public int hashCode() {
        return Objects.hash(this.userId, this.displayName, this.canLogout, this.permissions);
    }
}
