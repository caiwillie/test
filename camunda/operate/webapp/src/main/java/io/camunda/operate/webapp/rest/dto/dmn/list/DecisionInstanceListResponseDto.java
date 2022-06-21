package io.camunda.operate.webapp.rest.dto.dmn.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DecisionInstanceListResponseDto {
   private List decisionInstances = new ArrayList();
   private long totalCount;

   public List getDecisionInstances() {
      return this.decisionInstances;
   }

   public DecisionInstanceListResponseDto setDecisionInstances(List decisionInstances) {
      this.decisionInstances = decisionInstances;
      return this;
   }

   public long getTotalCount() {
      return this.totalCount;
   }

   public DecisionInstanceListResponseDto setTotalCount(long totalCount) {
      this.totalCount = totalCount;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionInstanceListResponseDto that = (DecisionInstanceListResponseDto)o;
         return this.totalCount == that.totalCount && Objects.equals(this.decisionInstances, that.decisionInstances);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.decisionInstances, this.totalCount});
   }
}
