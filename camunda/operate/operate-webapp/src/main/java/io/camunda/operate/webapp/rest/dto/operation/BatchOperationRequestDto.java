/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModelProperty
 */
package io.camunda.operate.webapp.rest.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;

public class BatchOperationRequestDto {
    private Object[] searchBefore;
    private Object[] searchAfter;
    private Integer pageSize;

    public BatchOperationRequestDto() {
    }

    public BatchOperationRequestDto(Integer pageSize, Object[] searchAfter, Object[] searchBefore) {
        this.pageSize = pageSize;
        this.searchAfter = searchAfter;
        this.searchBefore = searchBefore;
    }

    @ApiModelProperty(value="Array of two strings: copy/paste of sortValues field from one of the operations.", example="[\"9223372036854775807\", \"1583836503404\"]")
    public Object[] getSearchBefore() {
        return this.searchBefore;
    }

    public BatchOperationRequestDto setSearchBefore(Object[] searchBefore) {
        this.searchBefore = searchBefore;
        return this;
    }

    @ApiModelProperty(value="Array of two strings: copy/paste of sortValues field from one of the operations.", example="[\"1583836151645\", \"1583836128180\"]")
    public Object[] getSearchAfter() {
        return this.searchAfter;
    }

    public BatchOperationRequestDto setSearchAfter(Object[] searchAfter) {
        this.searchAfter = searchAfter;
        return this;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public BatchOperationRequestDto setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
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
        BatchOperationRequestDto that = (BatchOperationRequestDto)o;
        if (!Arrays.equals(this.searchBefore, that.searchBefore)) {
            return false;
        }
        if (Arrays.equals(this.searchAfter, that.searchAfter)) return this.pageSize != null ? this.pageSize.equals(that.pageSize) : that.pageSize == null;
        return false;
    }

    public int hashCode() {
        int result = Arrays.hashCode(this.searchBefore);
        result = 31 * result + Arrays.hashCode(this.searchAfter);
        result = 31 * result + (this.pageSize != null ? this.pageSize.hashCode() : 0);
        return result;
    }
}
