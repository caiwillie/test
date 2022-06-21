package io.camunda.operate.webapp.rest.dto.activity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FlowNodeInstanceRequestDto {
   private List queries;

   public FlowNodeInstanceRequestDto() {
   }

   public FlowNodeInstanceRequestDto(List queries) {
      this.queries = queries;
   }

   public FlowNodeInstanceRequestDto(FlowNodeInstanceQueryDto... queries) {
      this.queries = Arrays.asList(queries);
   }

   public List getQueries() {
      return this.queries;
   }

   public FlowNodeInstanceRequestDto setQueries(List queries) {
      this.queries = queries;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FlowNodeInstanceRequestDto that = (FlowNodeInstanceRequestDto)o;
         return Objects.equals(this.queries, that.queries);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.queries});
   }
}
