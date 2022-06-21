package io.camunda.operate.webapp.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class PaginatedQuery {
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

   public PaginatedQuery setSorting(SortingDto sorting) {
      if (sorting != null && !this.getValidSortByValues().contains(sorting.getSortBy())) {
         throw new InvalidRequestException("SortBy parameter has invalid value: " + sorting.getSortBy());
      } else {
         this.sorting = sorting;
         return this;
      }
   }

   @JsonIgnore
   protected Set getValidSortByValues() {
      return new HashSet();
   }

   @ApiModelProperty(
      value = "Array of values (can be one): copy/paste of sortValues field from one of the objects.",
      example = "[1605160098477, 4629710542312628000]"
   )
   public Object[] getSearchAfter() {
      return this.searchAfter;
   }

   public PaginatedQuery setSearchAfter(Object[] searchAfter) {
      this.searchAfter = searchAfter;
      return this;
   }

   public Object[] getSearchAfterOrEqual() {
      return this.searchAfterOrEqual;
   }

   public PaginatedQuery setSearchAfterOrEqual(Object[] searchAfterOrEqual) {
      this.searchAfterOrEqual = searchAfterOrEqual;
      return this;
   }

   @ApiModelProperty(
      value = "Array of values (can be one): copy/paste of sortValues field from one of the objects.",
      example = "[1605160098477, 4629710542312628000]"
   )
   public Object[] getSearchBefore() {
      return this.searchBefore;
   }

   public PaginatedQuery setSearchBefore(Object[] searchBefore) {
      this.searchBefore = searchBefore;
      return this;
   }

   public Object[] getSearchBeforeOrEqual() {
      return this.searchBeforeOrEqual;
   }

   public PaginatedQuery setSearchBeforeOrEqual(Object[] searchBeforeOrEqual) {
      this.searchBeforeOrEqual = searchBeforeOrEqual;
      return this;
   }

   public Integer getPageSize() {
      return this.pageSize;
   }

   public PaginatedQuery setPageSize(Integer pageSize) {
      this.pageSize = pageSize;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         PaginatedQuery that = (PaginatedQuery)o;
         return Objects.equals(this.sorting, that.sorting) && Arrays.equals(this.searchAfter, that.searchAfter) && Arrays.equals(this.searchAfterOrEqual, that.searchAfterOrEqual) && Arrays.equals(this.searchBefore, that.searchBefore) && Arrays.equals(this.searchBeforeOrEqual, that.searchBeforeOrEqual) && Objects.equals(this.pageSize, that.pageSize);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{this.sorting, this.pageSize});
      result = 31 * result + Arrays.hashCode(this.searchAfter);
      result = 31 * result + Arrays.hashCode(this.searchAfterOrEqual);
      result = 31 * result + Arrays.hashCode(this.searchBefore);
      result = 31 * result + Arrays.hashCode(this.searchBeforeOrEqual);
      return result;
   }
}
