/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.ErrorType
 */
package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.entities.ErrorType;
import java.util.Objects;

public class ErrorTypeDto
implements Comparable<ErrorTypeDto> {
    private String id;
    private String name;

    public String getId() {
        return this.id;
    }

    public ErrorTypeDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public ErrorTypeDto setName(String name) {
        this.name = name;
        return this;
    }

    public static ErrorTypeDto createFrom(ErrorType errorType) {
        return new ErrorTypeDto().setId(errorType.name()).setName(errorType.getTitle());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        ErrorTypeDto that = (ErrorTypeDto)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.name);
    }

    public String toString() {
        return "ErrorTypeDto{id='" + this.id + "', name='" + this.name + "'}";
    }

    @Override
    public int compareTo(ErrorTypeDto o) {
        if (this.id == null) return 0;
        return this.id.compareTo(o.getId());
    }
}
