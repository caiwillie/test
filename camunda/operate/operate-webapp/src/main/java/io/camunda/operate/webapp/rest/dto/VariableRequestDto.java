/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.PaginatedQuery
 *  io.camunda.operate.webapp.rest.dto.SortingDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.webapp.rest.dto.PaginatedQuery;
import io.camunda.operate.webapp.rest.dto.SortingDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

@ApiModel(value="Process instances variables request")
public class VariableRequestDto
extends PaginatedQuery<VariableRequestDto> {
    @ApiModelProperty(value="Variable scope. Must be processInstanceId for process instance level variables.")
    private String scopeId;

    public String getScopeId() {
        return this.scopeId;
    }

    public VariableRequestDto setScopeId(String scopeId) {
        this.scopeId = scopeId;
        return this;
    }

    public VariableRequestDto setSorting(SortingDto sorting) {
        if (sorting == null) return this;
        throw new InvalidRequestException("Sorting is not supported.");
    }

    public VariableRequestDto createCopy() {
        return ((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)new VariableRequestDto().setSearchBefore(this.getSearchBefore())).setSearchAfter(this.getSearchAfter())).setPageSize(this.getPageSize())).setSearchAfterOrEqual(this.getSearchAfterOrEqual())).setSearchBeforeOrEqual(this.getSearchBeforeOrEqual())).setScopeId(this.scopeId);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (((Object)((Object)this)).getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        VariableRequestDto that = (VariableRequestDto)((Object)o);
        return Objects.equals(this.scopeId, that.scopeId);
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), this.scopeId);
    }

    public String toString() {
        return "VariableRequestDto{scopeId='" + this.scopeId + "'}";
    }
}
