/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceQueryDto
 */
package io.camunda.operate.webapp.rest.dto.activity;

import io.camunda.operate.webapp.rest.dto.activity.FlowNodeInstanceQueryDto;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FlowNodeInstanceRequestDto {
    private List<FlowNodeInstanceQueryDto> queries;

    public FlowNodeInstanceRequestDto() {
    }

    public FlowNodeInstanceRequestDto(List<FlowNodeInstanceQueryDto> queries) {
        this.queries = queries;
    }

    public FlowNodeInstanceRequestDto(FlowNodeInstanceQueryDto ... queries) {
        this.queries = Arrays.asList(queries);
    }

    public List<FlowNodeInstanceQueryDto> getQueries() {
        return this.queries;
    }

    public FlowNodeInstanceRequestDto setQueries(List<FlowNodeInstanceQueryDto> queries) {
        this.queries = queries;
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
        FlowNodeInstanceRequestDto that = (FlowNodeInstanceRequestDto)o;
        return Objects.equals(this.queries, that.queries);
    }

    public int hashCode() {
        return Objects.hash(this.queries);
    }
}
