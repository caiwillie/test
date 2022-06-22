/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.ErrorType
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.entities.ErrorType;
import java.util.Objects;

public class IncidentErrorTypeDto {
    private String id;
    private String name;
    private int count;

    public String getId() {
        return this.id;
    }

    public IncidentErrorTypeDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public IncidentErrorTypeDto setName(String name) {
        this.name = name;
        return this;
    }

    public int getCount() {
        return this.count;
    }

    public IncidentErrorTypeDto setCount(int count) {
        this.count = count;
        return this;
    }

    public static IncidentErrorTypeDto createFrom(ErrorType errorType) {
        return new IncidentErrorTypeDto().setId(errorType.name()).setName(errorType.getTitle());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IncidentErrorTypeDto that = (IncidentErrorTypeDto)o;
        return this.count == that.count && Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.name, this.count);
    }
}
