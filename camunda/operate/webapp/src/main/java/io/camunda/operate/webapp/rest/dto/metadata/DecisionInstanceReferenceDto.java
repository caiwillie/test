package io.camunda.operate.webapp.rest.dto.metadata;

import java.util.Objects;

public class DecisionInstanceReferenceDto {
   private String instanceId;
   private String decisionName;

   public String getInstanceId() {
      return this.instanceId;
   }

   public DecisionInstanceReferenceDto setInstanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
   }

   public String getDecisionName() {
      return this.decisionName;
   }

   public DecisionInstanceReferenceDto setDecisionName(String decisionName) {
      this.decisionName = decisionName;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionInstanceReferenceDto that = (DecisionInstanceReferenceDto)o;
         return Objects.equals(this.instanceId, that.instanceId) && Objects.equals(this.decisionName, that.decisionName);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.instanceId, this.decisionName});
   }
}
