/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.management.dto;

import java.util.Objects;

public class UsageMetricDTO {
    private long total;

    public long getTotal() {
        return this.total;
    }

    public UsageMetricDTO setTotal(long total) {
        this.total = total;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UsageMetricDTO)) {
            return false;
        }
        UsageMetricDTO that = (UsageMetricDTO)o;
        return this.total == that.total;
    }

    public int hashCode() {
        return Objects.hash(this.total);
    }

    public String toString() {
        return "UsageMetricDTO{total=" + this.total + "}";
    }
}
