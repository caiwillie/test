/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.dmn.DecisionInstanceOutputEntity
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 */
package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.DecisionInstanceOutputEntity;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import java.util.Objects;

public class DecisionInstanceOutputDto
implements CreatableFromEntity<DecisionInstanceOutputDto, DecisionInstanceOutputEntity> {
    private String id;
    private String name;
    private String value;
    private String ruleId;
    private int ruleIndex;

    public String getId() {
        return this.id;
    }

    public DecisionInstanceOutputDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public DecisionInstanceOutputDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public DecisionInstanceOutputDto setValue(String value) {
        this.value = value;
        return this;
    }

    public String getRuleId() {
        return this.ruleId;
    }

    public DecisionInstanceOutputDto setRuleId(String ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public int getRuleIndex() {
        return this.ruleIndex;
    }

    public DecisionInstanceOutputDto setRuleIndex(int ruleIndex) {
        this.ruleIndex = ruleIndex;
        return this;
    }

    public DecisionInstanceOutputDto fillFrom(DecisionInstanceOutputEntity outputEntity) {
        return this.setId(outputEntity.getId()).setName(outputEntity.getName()).setValue(outputEntity.getValue()).setRuleId(outputEntity.getRuleId()).setRuleIndex(outputEntity.getRuleIndex());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        DecisionInstanceOutputDto that = (DecisionInstanceOutputDto)o;
        return this.ruleIndex == that.ruleIndex && Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value) && Objects.equals(this.ruleId, that.ruleId);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.name, this.value, this.ruleId, this.ruleIndex);
    }
}
