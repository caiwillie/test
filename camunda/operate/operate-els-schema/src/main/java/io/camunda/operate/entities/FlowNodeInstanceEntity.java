package io.camunda.operate.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

public class FlowNodeInstanceEntity extends OperateZeebeEntity {
   private String flowNodeId;
   private OffsetDateTime startDate;
   private OffsetDateTime endDate;
   private FlowNodeState state;
   private FlowNodeType type;
   private Long incidentKey;
   private Long processInstanceKey;
   private String treePath;
   private int level;
   private Long position;
   private boolean incident;
   @JsonIgnore
   private Object[] sortValues;

   public String getFlowNodeId() {
      return this.flowNodeId;
   }

   public void setFlowNodeId(String flowNodeId) {
      this.flowNodeId = flowNodeId;
   }

   public OffsetDateTime getStartDate() {
      return this.startDate;
   }

   public void setStartDate(OffsetDateTime startDate) {
      this.startDate = startDate;
   }

   public OffsetDateTime getEndDate() {
      return this.endDate;
   }

   public void setEndDate(OffsetDateTime endDate) {
      this.endDate = endDate;
   }

   public FlowNodeState getState() {
      return this.state;
   }

   public void setState(FlowNodeState state) {
      this.state = state;
   }

   public FlowNodeType getType() {
      return this.type;
   }

   public void setType(FlowNodeType type) {
      this.type = type;
   }

   public Long getIncidentKey() {
      return this.incidentKey;
   }

   public void setIncidentKey(Long incidentKey) {
      this.incidentKey = incidentKey;
   }

   public String getTreePath() {
      return this.treePath;
   }

   public void setTreePath(String treePath) {
      this.treePath = treePath;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public void setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
   }

   public Long getPosition() {
      return this.position;
   }

   public void setPosition(Long position) {
      this.position = position;
   }

   public boolean isIncident() {
      return this.incident;
   }

   public FlowNodeInstanceEntity setIncident(boolean incident) {
      this.incident = incident;
      return this;
   }

   public Object[] getSortValues() {
      return this.sortValues;
   }

   public void setSortValues(Object[] sortValues) {
      this.sortValues = sortValues;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            FlowNodeInstanceEntity that = (FlowNodeInstanceEntity)o;
            return this.level == that.level && this.incident == that.incident && Objects.equals(this.flowNodeId, that.flowNodeId) && Objects.equals(this.startDate, that.startDate) && Objects.equals(this.endDate, that.endDate) && this.state == that.state && this.type == that.type && Objects.equals(this.incidentKey, that.incidentKey) && Objects.equals(this.processInstanceKey, that.processInstanceKey) && Objects.equals(this.treePath, that.treePath) && Objects.equals(this.position, that.position) && Arrays.equals(this.sortValues, that.sortValues);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{super.hashCode(), this.flowNodeId, this.startDate, this.endDate, this.state, this.type, this.incidentKey, this.processInstanceKey, this.treePath, this.level, this.position, this.incident});
      result = 31 * result + Arrays.hashCode(this.sortValues);
      return result;
   }
}
