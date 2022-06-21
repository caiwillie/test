package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

@ApiModel("Process instances variables request")
public class VariableRequestDto extends PaginatedQuery {
   @ApiModelProperty("Variable scope. Must be processInstanceId for process instance level variables.")
   private String scopeId;

   public String getScopeId() {
      return this.scopeId;
   }

   public VariableRequestDto setScopeId(String scopeId) {
      this.scopeId = scopeId;
      return this;
   }

   public VariableRequestDto setSorting(SortingDto sorting) {
      if (sorting != null) {
         throw new InvalidRequestException("Sorting is not supported.");
      } else {
         return this;
      }
   }

   public VariableRequestDto createCopy() {
      return ((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)((VariableRequestDto)(new VariableRequestDto()).setSearchBefore(this.getSearchBefore())).setSearchAfter(this.getSearchAfter())).setPageSize(this.getPageSize())).setSearchAfterOrEqual(this.getSearchAfterOrEqual())).setSearchBeforeOrEqual(this.getSearchBeforeOrEqual())).setScopeId(this.scopeId);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            VariableRequestDto that = (VariableRequestDto)o;
            return Objects.equals(this.scopeId, that.scopeId);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.scopeId});
   }

   public String toString() {
      return "VariableRequestDto{scopeId='" + this.scopeId + "'}";
   }
}
