package io.camunda.operate.webapp.rest.dto.listview;

import io.camunda.operate.webapp.rest.dto.PaginatedQuery;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.ApiModel;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ApiModel("Process instances request")
public class ListViewRequestDto extends PaginatedQuery {
   public static final String SORT_BY_ID = "id";
   public static final String SORT_BY_START_DATE = "startDate";
   public static final String SORT_BY_END_DATE = "endDate";
   public static final String SORT_BY_PROCESS_NAME = "processName";
   public static final String SORT_BY_WORFLOW_VERSION = "processVersion";
   public static final String SORT_BY_PARENT_INSTANCE_ID = "parentInstanceId";
   public static final Set VALID_SORT_BY_VALUES = new HashSet();
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

   protected Set getValidSortByValues() {
      return VALID_SORT_BY_VALUES;
   }

   public ListViewRequestDto setSearchAfterOrEqual(Object[] searchAfterOrEqual) {
      if (searchAfterOrEqual != null) {
         throw new InvalidRequestException("SearchAfterOrEqual is not supported.");
      } else {
         return this;
      }
   }

   public ListViewRequestDto setSearchBeforeOrEqual(Object[] searchBeforeOrEqual) {
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
            ListViewRequestDto that = (ListViewRequestDto)o;
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
      VALID_SORT_BY_VALUES.add("startDate");
      VALID_SORT_BY_VALUES.add("endDate");
      VALID_SORT_BY_VALUES.add("processName");
      VALID_SORT_BY_VALUES.add("processVersion");
      VALID_SORT_BY_VALUES.add("parentInstanceId");
   }
}
