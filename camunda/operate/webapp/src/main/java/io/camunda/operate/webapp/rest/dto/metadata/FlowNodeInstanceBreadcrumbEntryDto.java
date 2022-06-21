package io.camunda.operate.webapp.rest.dto.metadata;

import io.camunda.operate.entities.FlowNodeType;
import java.util.Objects;

public class FlowNodeInstanceBreadcrumbEntryDto {
   private String flowNodeId;
   private FlowNodeType flowNodeType;

   public FlowNodeInstanceBreadcrumbEntryDto() {
   }

   public FlowNodeInstanceBreadcrumbEntryDto(String flowNodeId, FlowNodeType flowNodeType) {
      this.flowNodeId = flowNodeId;
      this.flowNodeType = flowNodeType;
   }

   public String getFlowNodeId() {
      return this.flowNodeId;
   }

   public FlowNodeInstanceBreadcrumbEntryDto setFlowNodeId(String flowNodeId) {
      this.flowNodeId = flowNodeId;
      return this;
   }

   public FlowNodeType getFlowNodeType() {
      return this.flowNodeType;
   }

   public FlowNodeInstanceBreadcrumbEntryDto setFlowNodeType(FlowNodeType flowNodeType) {
      this.flowNodeType = flowNodeType;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FlowNodeInstanceBreadcrumbEntryDto that = (FlowNodeInstanceBreadcrumbEntryDto)o;
         return Objects.equals(this.flowNodeId, that.flowNodeId) && this.flowNodeType == that.flowNodeType;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.flowNodeId, this.flowNodeType});
   }

   public String toString() {
      return "FlowNodeInstanceBreadcrumbEntryDto{flowNodeId='" + this.flowNodeId + "', flowNodeType=" + this.flowNodeType + "}";
   }
}
