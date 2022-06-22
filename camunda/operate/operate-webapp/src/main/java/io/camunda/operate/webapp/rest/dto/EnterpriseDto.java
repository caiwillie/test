/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.rest.dto;

import java.util.Objects;

public class EnterpriseDto {
    private final boolean enterprise;

    public EnterpriseDto(boolean enterprise) {
        this.enterprise = enterprise;
    }

    public boolean isEnterprise() {
        return this.enterprise;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        EnterpriseDto that = (EnterpriseDto)o;
        return this.enterprise == that.enterprise;
    }

    public int hashCode() {
        return Objects.hash(this.enterprise);
    }

    public String toString() {
        return "EnterpriseDto{enterprise=" + this.enterprise + "}";
    }
}
