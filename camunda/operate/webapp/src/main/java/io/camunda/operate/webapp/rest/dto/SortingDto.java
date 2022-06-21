package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

@ApiModel("Sorting")
public class SortingDto {
   public static final String SORT_ORDER_ASC_VALUE = "asc";
   public static final String SORT_ORDER_DESC_VALUE = "desc";
   public static final List VALID_SORT_ORDER_VALUES = new ArrayList();
   private String sortBy;
   private String sortOrder = "asc";

   @ApiModelProperty(
      value = "Data field to sort by",
      required = true
   )
   public String getSortBy() {
      return this.sortBy;
   }

   public void setSortBy(String sortBy) {
      this.sortBy = sortBy;
   }

   @ApiModelProperty(
      value = "Sort order, default: asc",
      allowableValues = "asc,desc",
      required = false
   )
   public String getSortOrder() {
      return this.sortOrder;
   }

   public void setSortOrder(String sortOrder) {
      if (!VALID_SORT_ORDER_VALUES.contains(sortOrder)) {
         throw new InvalidRequestException("SortOrder parameter has invalid value: " + sortOrder);
      } else {
         this.sortOrder = sortOrder;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SortingDto that = (SortingDto)o;
         if (this.sortBy != null) {
            if (this.sortBy.equals(that.sortBy)) {
               return this.sortOrder != null ? this.sortOrder.equals(that.sortOrder) : that.sortOrder == null;
            }
         } else if (that.sortBy == null) {
            return this.sortOrder != null ? this.sortOrder.equals(that.sortOrder) : that.sortOrder == null;
         }

         return false;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.sortBy != null ? this.sortBy.hashCode() : 0;
      result = 31 * result + (this.sortOrder != null ? this.sortOrder.hashCode() : 0);
      return result;
   }

   static {
      VALID_SORT_ORDER_VALUES.add("asc");
      VALID_SORT_ORDER_VALUES.add("desc");
   }
}
