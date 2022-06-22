/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.dmn.DecisionInstanceEntity
 *  io.camunda.operate.entities.dmn.DecisionInstanceState
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 *  io.camunda.operate.webapp.rest.dto.dmn.DecisionInstanceStateDto
 */
package io.camunda.operate.webapp.rest.dto.dmn.list;

import io.camunda.operate.entities.dmn.DecisionInstanceEntity;
import io.camunda.operate.entities.dmn.DecisionInstanceState;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import io.camunda.operate.webapp.rest.dto.dmn.DecisionInstanceStateDto;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

public class DecisionInstanceForListDto
implements CreatableFromEntity<DecisionInstanceForListDto, DecisionInstanceEntity> {
    private String id;
    private DecisionInstanceStateDto state;
    private String decisionName;
    private Integer decisionVersion;
    private OffsetDateTime evaluationDate;
    private String processInstanceId;
    private String[] sortValues;

    public String getId() {
        return this.id;
    }

    public DecisionInstanceStateDto getState() {
        return this.state;
    }

    public DecisionInstanceForListDto setState(DecisionInstanceStateDto state) {
        this.state = state;
        return this;
    }

    public DecisionInstanceForListDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getDecisionName() {
        return this.decisionName;
    }

    public DecisionInstanceForListDto setDecisionName(String decisionName) {
        this.decisionName = decisionName;
        return this;
    }

    public Integer getDecisionVersion() {
        return this.decisionVersion;
    }

    public DecisionInstanceForListDto setDecisionVersion(Integer decisionVersion) {
        this.decisionVersion = decisionVersion;
        return this;
    }

    public OffsetDateTime getEvaluationDate() {
        return this.evaluationDate;
    }

    public DecisionInstanceForListDto setEvaluationDate(OffsetDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
        return this;
    }

    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    public DecisionInstanceForListDto setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public String[] getSortValues() {
        return this.sortValues;
    }

    public DecisionInstanceForListDto setSortValues(String[] sortValues) {
        this.sortValues = sortValues;
        return this;
    }

    public DecisionInstanceForListDto fillFrom(DecisionInstanceEntity entity) {
        return this.setDecisionName(entity.getDecisionName()).setDecisionVersion(entity.getDecisionVersion()).setEvaluationDate(entity.getEvaluationDate()).setId(entity.getId()).setProcessInstanceId(String.valueOf(entity.getProcessInstanceKey())).setState(DecisionInstanceStateDto.getState((DecisionInstanceState)entity.getState())).setSortValues((String[])Arrays.stream(entity.getSortValues()).map(String::valueOf).toArray(String[]::new));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        DecisionInstanceForListDto that = (DecisionInstanceForListDto)o;
        return Objects.equals(this.id, that.id) && this.state == that.state && Objects.equals(this.decisionName, that.decisionName) && Objects.equals(this.decisionVersion, that.decisionVersion) && Objects.equals(this.evaluationDate, that.evaluationDate) && Objects.equals(this.processInstanceId, that.processInstanceId) && Arrays.equals(this.sortValues, that.sortValues);
    }

    public int hashCode() {
        int result = Objects.hash(this.id, this.state, this.decisionName, this.decisionVersion, this.evaluationDate, this.processInstanceId);
        result = 31 * result + Arrays.hashCode(this.sortValues);
        return result;
    }

    public String toString() {
        return "DecisionInstanceForListDto{id='" + this.id + "', state=" + this.state + ", decisionName='" + this.decisionName + "', decisionVersion=" + this.decisionVersion + ", evaluationDate=" + this.evaluationDate + ", processInstanceId='" + this.processInstanceId + "', sortValues=" + Arrays.toString(this.sortValues) + "}";
    }
}
