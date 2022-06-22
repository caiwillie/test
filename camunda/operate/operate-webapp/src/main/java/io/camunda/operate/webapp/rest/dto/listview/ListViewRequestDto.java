/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.PaginatedQuery
 *  io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 *  io.swagger.annotations.ApiModel
 */
package io.camunda.operate.webapp.rest.dto.listview;

import io.camunda.operate.webapp.rest.dto.PaginatedQuery;
import io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.ApiModel;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ApiModel(value="Process instances request")
public class ListViewRequestDto
extends PaginatedQuery<ListViewRequestDto> {
    public static final String SORT_BY_ID = "id";
    public static final String SORT_BY_START_DATE = "startDate";
    public static final String SORT_BY_END_DATE = "endDate";
    public static final String SORT_BY_PROCESS_NAME = "processName";
    public static final String SORT_BY_WORFLOW_VERSION = "processVersion";
    public static final String SORT_BY_PARENT_INSTANCE_ID = "parentInstanceId";
    public static final Set<String> VALID_SORT_BY_VALUES = new HashSet<String>();
    private ListViewQueryDto query;

    public ListViewRequestDto() {
    }

    public ListViewRequestDto(ListViewQueryDto query) {
        this.query = query;
    }

    public ListViewQueryDto getQuery() {
        return this.query;
    }

    public void setQuery(ListViewQueryDto query) {
        this.query = query;
    }

    protected Set<String> getValidSortByValues() {
        return VALID_SORT_BY_VALUES;
    }

    public ListViewRequestDto setSearchAfterOrEqual(Object[] searchAfterOrEqual) {
        if (searchAfterOrEqual == null) return this;
        throw new InvalidRequestException("SearchAfterOrEqual is not supported.");
    }

    public ListViewRequestDto setSearchBeforeOrEqual(Object[] searchBeforeOrEqual) {
        if (searchBeforeOrEqual == null) return this;
        throw new InvalidRequestException("SearchBeforeOrEqual is not supported.");
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
        ListViewRequestDto that = (ListViewRequestDto)((Object)o);
        return Objects.equals(this.query, that.query);
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), this.query);
    }

    static {
        VALID_SORT_BY_VALUES.add(SORT_BY_ID);
        VALID_SORT_BY_VALUES.add(SORT_BY_START_DATE);
        VALID_SORT_BY_VALUES.add(SORT_BY_END_DATE);
        VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_NAME);
        VALID_SORT_BY_VALUES.add(SORT_BY_WORFLOW_VERSION);
        VALID_SORT_BY_VALUES.add(SORT_BY_PARENT_INSTANCE_ID);
    }
}
