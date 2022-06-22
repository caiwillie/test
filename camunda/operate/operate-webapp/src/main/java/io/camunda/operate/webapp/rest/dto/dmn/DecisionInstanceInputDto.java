/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.dmn.DecisionInstanceInputEntity
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 */
package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.DecisionInstanceInputEntity;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import java.util.Objects;

public class DecisionInstanceInputDto
implements CreatableFromEntity<DecisionInstanceInputDto, DecisionInstanceInputEntity> {
    private String id;
    private String name;
    private String value;

    public String getId() {
        return this.id;
    }

    public DecisionInstanceInputDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public DecisionInstanceInputDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public DecisionInstanceInputDto setValue(String value) {
        this.value = value;
        return this;
    }

    public DecisionInstanceInputDto fillFrom(DecisionInstanceInputEntity inputEntity) {
        return this.setId(inputEntity.getId()).setName(inputEntity.getName()).setValue(inputEntity.getValue());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        DecisionInstanceInputDto that = (DecisionInstanceInputDto)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.name, this.value);
    }
}
