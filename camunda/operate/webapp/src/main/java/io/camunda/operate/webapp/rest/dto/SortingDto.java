/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value="Sorting")
public class SortingDto {
    public static final String SORT_ORDER_ASC_VALUE = "asc";
    public static final String SORT_ORDER_DESC_VALUE = "desc";
    public static final List<String> VALID_SORT_ORDER_VALUES = new ArrayList<String>();
    private String sortBy;
    private String sortOrder = "asc";

    @ApiModelProperty(value="Data field to sort by", required=true)
    public String getSortBy() {
        return this.sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    @ApiModelProperty(value="Sort order, default: asc", allowableValues="asc,desc", required=false)
    public String getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        if (!VALID_SORT_ORDER_VALUES.contains(sortOrder)) {
            throw new InvalidRequestException("SortOrder parameter has invalid value: " + sortOrder);
        }
        this.sortOrder = sortOrder;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        SortingDto that = (SortingDto)o;
        if (!(this.sortBy != null ? !this.sortBy.equals(that.sortBy) : that.sortBy != null)) return this.sortOrder != null ? this.sortOrder.equals(that.sortOrder) : that.sortOrder == null;
        return false;
    }

    public int hashCode() {
        int result = this.sortBy != null ? this.sortBy.hashCode() : 0;
        result = 31 * result + (this.sortOrder != null ? this.sortOrder.hashCode() : 0);
        return result;
    }

    static {
        VALID_SORT_ORDER_VALUES.add(SORT_ORDER_ASC_VALUE);
        VALID_SORT_ORDER_VALUES.add(SORT_ORDER_DESC_VALUE);
    }
}
