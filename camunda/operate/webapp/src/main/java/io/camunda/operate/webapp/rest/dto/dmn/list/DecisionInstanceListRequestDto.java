package io.camunda.operate.webapp.rest.dto.dmn.list;

import io.camunda.operate.webapp.rest.dto.PaginatedQuery;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DecisionInstanceListRequestDto extends PaginatedQuery {
   public static final String SORT_BY_ID = "id";
   public static final String SORT_BY_DECISION_NAME = "decisionName";
   public static final String SORT_BY_DECISION_VERSION = "decisionVersion";
   public static final String SORT_BY_EVALUATION_DATE = "evaluationDate";
   public static final String SORT_BY_PROCESS_INSTANCE_ID = "processInstanceId";
   public static final Set VALID_SORT_BY_VALUES = new HashSet();
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

   protected Set getValidSortByValues() {
      return VALID_SORT_BY_VALUES;
   }

   public DecisionInstanceListRequestDto setSearchAfterOrEqual(Object[] searchAfterOrEqual) {
      if (searchAfterOrEqual != null) {
         throw new InvalidRequestException("SearchAfterOrEqual is not supported.");
      } else {
         return this;
      }
   }

   public DecisionInstanceListRequestDto setSearchBeforeOrEqual(Object[] searchBeforeOrEqual) {
      if (searchBeforeOrEqual != null) {
         throw new InvalidRequestException("SearchBeforeOrEqual is not supported.");
      } else {
         return this;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            DecisionInstanceListRequestDto that = (DecisionInstanceListRequestDto)o;
            return Objects.equals(this.query, that.query);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.query});
   }

   static {
      VALID_SORT_BY_VALUES.add("id");
      VALID_SORT_BY_VALUES.add("decisionName");
      VALID_SORT_BY_VALUES.add("decisionVersion");
      VALID_SORT_BY_VALUES.add("evaluationDate");
      VALID_SORT_BY_VALUES.add("processInstanceId");
   }
}
