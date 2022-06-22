/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.rest.dto.PaginatedQuery
 *  io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListQueryDto
 *  io.camunda.operate.webapp.rest.exception.InvalidRequestException
 */
package io.camunda.operate.webapp.rest.dto.dmn.list;

import io.camunda.operate.webapp.rest.dto.PaginatedQuery;
import io.camunda.operate.webapp.rest.dto.dmn.list.DecisionInstanceListQueryDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DecisionInstanceListRequestDto
extends PaginatedQuery<DecisionInstanceListRequestDto> {
    public static final String SORT_BY_ID = "id";
    public static final String SORT_BY_DECISION_NAME = "decisionName";
    public static final String SORT_BY_DECISION_VERSION = "decisionVersion";
    public static final String SORT_BY_EVALUATION_DATE = "evaluationDate";
    public static final String SORT_BY_PROCESS_INSTANCE_ID = "processInstanceId";
    public static final Set<String> VALID_SORT_BY_VALUES = new HashSet<String>();
    private DecisionInstanceListQueryDto query;

    public DecisionInstanceListRequestDto() {
    }

    public DecisionInstanceListRequestDto(DecisionInstanceListQueryDto query) {
        this.query = query;
    }

    public DecisionInstanceListQueryDto getQuery() {
        return this.query;
    }

    public DecisionInstanceListRequestDto setQuery(DecisionInstanceListQueryDto query) {
        this.query = query;
        return this;
    }

    protected Set<String> getValidSortByValues() {
        return VALID_SORT_BY_VALUES;
    }

    public DecisionInstanceListRequestDto setSearchAfterOrEqual(Object[] searchAfterOrEqual) {
        if (searchAfterOrEqual == null) return this;
        throw new InvalidRequestException("SearchAfterOrEqual is not supported.");
    }

    public DecisionInstanceListRequestDto setSearchBeforeOrEqual(Object[] searchBeforeOrEqual) {
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
        DecisionInstanceListRequestDto that = (DecisionInstanceListRequestDto)((Object)o);
        return Objects.equals(this.query, that.query);
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), this.query);
    }

    static {
        VALID_SORT_BY_VALUES.add(SORT_BY_ID);
        VALID_SORT_BY_VALUES.add(SORT_BY_DECISION_NAME);
        VALID_SORT_BY_VALUES.add(SORT_BY_DECISION_VERSION);
        VALID_SORT_BY_VALUES.add(SORT_BY_EVALUATION_DATE);
        VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID);
    }
}
