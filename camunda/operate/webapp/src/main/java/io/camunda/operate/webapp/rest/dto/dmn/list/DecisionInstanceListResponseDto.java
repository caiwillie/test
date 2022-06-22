/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceForListDto
 */
package io.camunda.operate.webapp.rest.dto.dmn.list;

import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceForListDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DecisionInstanceListResponseDto {
    private List<DecisionInstanceForListDto> decisionInstances = new ArrayList<DecisionInstanceForListDto>();
    private long totalCount;

    public List<DecisionInstanceForListDto> getDecisionInstances() {
        return this.decisionInstances;
    }

    public DecisionInstanceListResponseDto setDecisionInstances(List<DecisionInstanceForListDto> decisionInstances) {
        this.decisionInstances = decisionInstances;
        return this;
    }

    public long getTotalCount() {
        return this.totalCount;
    }

    public DecisionInstanceListResponseDto setTotalCount(long totalCount) {
        this.totalCount = totalCount;
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
        DecisionInstanceListResponseDto that = (DecisionInstanceListResponseDto)o;
        return this.totalCount == that.totalCount && Objects.equals(this.decisionInstances, that.decisionInstances);
    }

    public int hashCode() {
        return Objects.hash(this.decisionInstances, this.totalCount);
    }
}
