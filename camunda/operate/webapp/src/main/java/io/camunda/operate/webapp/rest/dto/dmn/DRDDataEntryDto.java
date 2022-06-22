/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonIgnore
 *  io.camunda.operate.entities.dmn.DecisionInstanceState
 */
package io.camunda.operate.webapp.rest.dto.dmn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.operate.entities.dmn.DecisionInstanceState;
import java.util.Objects;

public class DRDDataEntryDto {
    @JsonIgnore
    private String decisionId;
    private String decisionInstanceId;
    private DecisionInstanceState state;

    public DRDDataEntryDto() {
    }

    public DRDDataEntryDto(String decisionInstanceId, String decisionId, DecisionInstanceState state) {
        this.decisionInstanceId = decisionInstanceId;
        this.decisionId = decisionId;
        this.state = state;
    }

    public String getDecisionId() {
        return this.decisionId;
    }

    public DRDDataEntryDto setDecisionId(String decisionId) {
        this.decisionId = decisionId;
        return this;
    }

    public String getDecisionInstanceId() {
        return this.decisionInstanceId;
    }

    public DRDDataEntryDto setDecisionInstanceId(String decisionInstanceId) {
        this.decisionInstanceId = decisionInstanceId;
        return this;
    }

    public DecisionInstanceState getState() {
        return this.state;
    }

    public DRDDataEntryDto setState(DecisionInstanceState state) {
        this.state = state;
        return this;
    }

    public int hashCode() {
        return Objects.hash(this.decisionId, this.decisionInstanceId, this.state);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        DRDDataEntryDto that = (DRDDataEntryDto)o;
        return Objects.equals(this.decisionId, that.decisionId) && Objects.equals(this.decisionInstanceId, that.decisionInstanceId) && this.state == that.state;
    }
}
