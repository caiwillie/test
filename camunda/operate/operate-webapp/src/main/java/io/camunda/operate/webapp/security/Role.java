/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.security;

import java.util.Arrays;

public enum Role {
    OWNER,
    OPERATOR,
    USER;


    public static Role fromString(String roleAsString) {
        String roleName = roleAsString.replaceAll("\\s+", "_");
        Role[] roleArray = Role.values();
        int n = roleArray.length;
        int n2 = 0;
        while (true) {
            if (n2 >= n) {
                throw new IllegalArgumentException(String.format("%s does not exists as Role in %s", roleAsString, Arrays.toString((Object[])Role.values())));
            }
            Role role = roleArray[n2];
            if (role.name().equalsIgnoreCase(roleName)) {
                return role;
            }
            ++n2;
        }
    }
}
