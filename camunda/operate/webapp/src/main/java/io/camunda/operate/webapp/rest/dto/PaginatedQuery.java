/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonIgnore
 *  io.camunda.operate.webapp.rest.dto.SortingDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  io.swagger.annotations.ApiModelProperty
 */
package io.camunda.operate.webapp.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.operate.webapp.rest.dto.SortingDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class PaginatedQuery<T extends PaginatedQuery<T>> {
    private static final int DEFAULT_PAGE_SIZE = 50;
    private SortingDto sorting;
    private Object[] searchAfter;
    private Object[] searchAfterOrEqual;
    private Object[] searchBefore;
    private Object[] searchBeforeOrEqual;
    protected Integer pageSize = 50;

    public SortingDto getSorting() {
        return this.sorting;
    }

    public T setSorting(SortingDto sorting) {
        if (sorting != null && !this.getValidSortByValues().contains(sorting.getSortBy())) {
            throw new InvalidRequestException("SortBy parameter has invalid value: " + sorting.getSortBy());
        }
        this.sorting = sorting;
        return (T)this;
    }

    @JsonIgnore
    protected Set<String> getValidSortByValues() {
        return new HashSet<String>();
    }

    @ApiModelProperty(value="Array of values (can be one): copy/paste of sortValues field from one of the objects.", example="[1605160098477, 4629710542312628000]")
    public Object[] getSearchAfter() {
        return this.searchAfter;
    }

    public T setSearchAfter(Object[] searchAfter) {
        this.searchAfter = searchAfter;
        return (T)this;
    }

    public Object[] getSearchAfterOrEqual() {
        return this.searchAfterOrEqual;
    }

    public T setSearchAfterOrEqual(Object[] searchAfterOrEqual) {
        this.searchAfterOrEqual = searchAfterOrEqual;
        return (T)this;
    }

    @ApiModelProperty(value="Array of values (can be one): copy/paste of sortValues field from one of the objects.", example="[1605160098477, 4629710542312628000]")
    public Object[] getSearchBefore() {
        return this.searchBefore;
    }

    public T setSearchBefore(Object[] searchBefore) {
        this.searchBefore = searchBefore;
        return (T)this;
    }

    public Object[] getSearchBeforeOrEqual() {
        return this.searchBeforeOrEqual;
    }

    public T setSearchBeforeOrEqual(Object[] searchBeforeOrEqual) {
        this.searchBeforeOrEqual = searchBeforeOrEqual;
        return (T)this;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public T setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return (T)this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        PaginatedQuery that = (PaginatedQuery)o;
        return Objects.equals(this.sorting, that.sorting) && Arrays.equals(this.searchAfter, that.searchAfter) && Arrays.equals(this.searchAfterOrEqual, that.searchAfterOrEqual) && Arrays.equals(this.searchBefore, that.searchBefore) && Arrays.equals(this.searchBeforeOrEqual, that.searchBeforeOrEqual) && Objects.equals(this.pageSize, that.pageSize);
    }

    public int hashCode() {
        int result = Objects.hash(this.sorting, this.pageSize);
        result = 31 * result + Arrays.hashCode(this.searchAfter);
        result = 31 * result + Arrays.hashCode(this.searchAfterOrEqual);
        result = 31 * result + Arrays.hashCode(this.searchBefore);
        result = 31 * result + Arrays.hashCode(this.searchBeforeOrEqual);
        return result;
    }
}
