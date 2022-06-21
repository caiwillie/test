package io.camunda.operate.webapp.rest.dto.metadata;

import io.camunda.operate.entities.FlowNodeType;
import io.camunda.operate.webapp.rest.dto.incidents.IncidentDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlowNodeMetadataDto {
   private String flowNodeInstanceId;
   private String flowNodeId;
   private FlowNodeType flowNodeType;
   private Long instanceCount;
   private List breadcrumb = new ArrayList();
   private FlowNodeInstanceMetadataDto instanceMetadata;
   private Long incidentCount;
   private IncidentDto incident;

   public String getFlowNodeInstanceId() {
      return this.flowNodeInstanceId;
   }

   public FlowNodeMetadataDto setFlowNodeInstanceId(String flowNodeInstanceId) {
      this.flowNodeInstanceId = flowNodeInstanceId;
      return this;
   }

   public String getFlowNodeId() {
      return this.flowNodeId;
   }

   public FlowNodeMetadataDto setFlowNodeId(String flowNodeId) {
      this.flowNodeId = flowNodeId;
      return this;
   }

   public FlowNodeType getFlowNodeType() {
      return this.flowNodeType;
   }

   public FlowNodeMetadataDto setFlowNodeType(FlowNodeType flowNodeType) {
      this.flowNodeType = flowNodeType;
      return this;
   }

   public Long getInstanceCount() {
      return this.instanceCount;
   }

   public FlowNodeMetadataDto setInstanceCount(Long instanceCount) {
      this.instanceCount = instanceCount;
      return this;
   }

   public List getBreadcrumb() {
      return this.breadcrumb;
   }

   public FlowNodeMetadataDto setBreadcrumb(List breadcrumb) {
      this.breadcrumb = breadcrumb;
      return this;
   }

   public FlowNodeInstanceMetadataDto getInstanceMetadata() {
      return this.instanceMetadata;
   }

   public FlowNodeMetadataDto setInstanceMetadata(FlowNodeInstanceMetadataDto instanceMetadata) {
      this.instanceMetadata = instanceMetadata;
      return this;
   }

   public Long getIncidentCount() {
      return this.incidentCount;
   }

   public FlowNodeMetadataDto setIncidentCount(Long incidentCount) {
      this.incidentCount = incidentCount;
      return this;
   }

   public IncidentDto getIncident() {
      return this.incident;
   }

   public FlowNodeMetadataDto setIncident(IncidentDto incident) {
      this.incident = incident;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FlowNodeMetadataDto that = (FlowNodeMetadataDto)o;
         return Objects.equals(this.flowNodeInstanceId, that.flowNodeInstanceId) && Objects.equals(this.flowNodeId, that.flowNodeId) && this.flowNodeType == that.flowNodeType && Objects.equals(this.instanceCount, that.instanceCount) && Objects.equals(this.breadcrumb, that.breadcrumb) && Objects.equals(this.instanceMetadata, that.instanceMetadata) && Objects.equals(this.incidentCount, that.incidentCount) && Objects.equals(this.incident, that.incident);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.flowNodeInstanceId, this.flowNodeId, this.flowNodeType, this.instanceCount, this.breadcrumb, this.instanceMetadata, this.incidentCount, this.incident});
   }

   public String toString() {
      return "FlowNodeMetadataDto{flowNodeInstanceId='" + this.flowNodeInstanceId + "', flowNodeId='" + this.flowNodeId + "', flowNodeType=" + this.flowNodeType + ", instanceCount=" + this.instanceCount + ", breadcrumb=" + this.breadcrumb + ", instanceMetadata=" + this.instanceMetadata + ", incidentCount=" + this.incidentCount + ", incident=" + this.incident + "}";
   }
}
