/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.util.CollectionUtil
 *  io.camunda.operate.webapp.security.Role
 *  org.springframework.security.core.GrantedAuthority
 *  org.springframework.security.core.authority.SimpleGrantedAuthority
 *  org.springframework.security.core.userdetails.User
 */
package io.camunda.operate.webapp.security.es;

import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.webapp.security.Role;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class User
extends org.springframework.security.core.userdetails.User {
    private String userId;
    private String displayName;
    private List<Role> roles;
    private boolean canLogout = true;

    private static Collection<? extends GrantedAuthority> toAuthorities(List<Role> roles) {
        return CollectionUtil.map(roles, role -> new SimpleGrantedAuthority("ROLE_" + role));
    }

    public User(String userId, String displayName, String password, List<Role> roles) {
        super(userId, password, User.toAuthorities(roles));
        this.userId = userId;
        this.displayName = displayName;
        this.roles = roles;
    }

    public String getUserId() {
        return this.userId;
    }

    public User setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public User setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public boolean isCanLogout() {
        return this.canLogout;
    }

    public User setCanLogout(boolean canLogout) {
        this.canLogout = canLogout;
        return this;
    }

    public User setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

    public List<Role> getRoles() {
        return this.roles;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (((Object)((Object)this)).getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        User user = (User)((Object)o);
        return this.canLogout == user.canLogout && this.userId.equals(user.userId) && this.displayName.equals(user.displayName) && this.roles.equals(user.roles);
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), this.userId, this.displayName, this.roles, this.canLogout);
    }
}
