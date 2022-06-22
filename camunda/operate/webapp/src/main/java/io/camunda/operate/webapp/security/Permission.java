/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.security;

import java.util.Arrays;

public enum Permission {
    READ,
    WRITE;


    public static Permission fromString(String permissionAsString) {
        Permission[] permissionArray = Permission.values();
        int n = permissionArray.length;
        int n2 = 0;
        while (true) {
            if (n2 >= n) {
                throw new IllegalArgumentException(String.format("%s does not exists as Permission in %s", permissionAsString, Arrays.toString((Object[])Permission.values())));
            }
            Permission permission = permissionArray[n2];
            if (permission.name().equalsIgnoreCase(permissionAsString)) {
                return permission;
            }
            ++n2;
        }
    }
}
