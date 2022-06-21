package io.camunda.operate.webapp.rest.dto.operation;

import io.camunda.operate.entities.OperationType;
import io.camunda.operate.webapp.rest.dto.listview.ListViewQueryDto;

public class CreateBatchOperationRequestDto {
   private ListViewQueryDto query;
   private OperationType operationType;
   private String name;

   public CreateBatchOperationRequestDto() {
   }

   public CreateBatchOperationRequestDto(ListViewQueryDto query, OperationType operationType) {
      this.query = query;
      this.operationType = operationType;
   }

   public ListViewQueryDto getQuery() {
      return this.query;
   }

   public CreateBatchOperationRequestDto setQuery(ListViewQueryDto query) {
      this.query = query;
      return this;
   }

   public OperationType getOperationType() {
      return this.operationType;
   }

   public CreateBatchOperationRequestDto setOperationType(OperationType operationType) {
      this.operationType = operationType;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public CreateBatchOperationRequestDto setName(String name) {
      this.name = name;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CreateBatchOperationRequestDto that = (CreateBatchOperationRequestDto)o;
         if (this.query != null) {
            if (!this.query.equals(that.query)) {
               return false;
            }
         } else if (that.query != null) {
            return false;
         }

         if (this.operationType != that.operationType) {
            return false;
         } else {
            return this.name != null ? this.name.equals(that.name) : that.name == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.query != null ? this.query.hashCode() : 0;
      result = 31 * result + (this.operationType != null ? this.operationType.hashCode() : 0);
      result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
      return result;
   }

   public String toString() {
      return "CreateBatchOperationRequestDto{query=" + this.query + ", operationType=" + this.operationType + ", name='" + this.name + "'}";
   }
}
