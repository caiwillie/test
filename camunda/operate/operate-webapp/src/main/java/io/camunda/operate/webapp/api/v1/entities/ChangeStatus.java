/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.api.v1.entities;

import java.util.Objects;

public class ChangeStatus {
    private String message;
    private Long deleted;

    public String getMessage() {
        return this.message;
    }

    public ChangeStatus setMessage(String message) {
        this.message = message;
        return this;
    }

    public Long getDeleted() {
        return this.deleted;
    }

    public ChangeStatus setDeleted(long deleted) {
        this.deleted = deleted;
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
        ChangeStatus status = (ChangeStatus)o;
        return this.deleted.equals(status.deleted) && Objects.equals(this.message, status.message);
    }

    public int hashCode() {
        return Objects.hash(this.message, this.deleted);
    }

    public String toString() {
        return "ChangeStatus{message='" + this.message + "', deleted=" + this.deleted + "}";
    }
}
