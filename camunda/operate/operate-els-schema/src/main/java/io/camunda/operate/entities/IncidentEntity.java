package io.camunda.operate.entities;

import java.time.OffsetDateTime;
import java.util.Objects;

public class IncidentEntity extends OperateZeebeEntity {
   private ErrorType errorType;
   private String errorMessage;
   private Integer errorMessageHash;
   private IncidentState state;
   private String flowNodeId;
   private Long flowNodeInstanceKey;
   private Long jobKey;
   private Long processInstanceKey;
   private OffsetDateTime creationTime;
   private Long processDefinitionKey;
   private String treePath;
   private boolean pending = true;

   public ErrorType getErrorType() {
      return this.errorType;
   }

   public IncidentEntity setErrorType(ErrorType errorType) {
      this.errorType = errorType;
      return this;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public IncidentEntity setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      this.setErrorMessageHash(errorMessage.hashCode());
      return this;
   }

   public void setErrorMessageHash(Integer errorMessageHash) {
      this.errorMessageHash = errorMessageHash;
   }

   public Integer getErrorMessageHash() {
      return this.errorMessage.hashCode();
   }

   public IncidentState getState() {
      return this.state;
   }

   public IncidentEntity setState(IncidentState state) {
      this.state = state;
      return this;
   }

   public String getFlowNodeId() {
      return this.flowNodeId;
   }

   public IncidentEntity setFlowNodeId(String flowNodeId) {
      this.flowNodeId = flowNodeId;
      return this;
   }

   public Long getFlowNodeInstanceKey() {
      return this.flowNodeInstanceKey;
   }

   public IncidentEntity setFlowNodeInstanceKey(Long flowNodeInstanceId) {
      this.flowNodeInstanceKey = flowNodeInstanceId;
      return this;
   }

   public Long getJobKey() {
      return this.jobKey;
   }

   public IncidentEntity setJobKey(Long jobKey) {
      this.jobKey = jobKey;
      return this;
   }

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public IncidentEntity setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
      return this;
   }

   public OffsetDateTime getCreationTime() {
      return this.creationTime;
   }

   public IncidentEntity setCreationTime(OffsetDateTime creationTime) {
      this.creationTime = creationTime;
      return this;
   }

   public IncidentEntity setProcessDefinitionKey(Long processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
      return this;
   }

   public Long getProcessDefinitionKey() {
      return this.processDefinitionKey;
   }

   public String getTreePath() {
      return this.treePath;
   }

   public IncidentEntity setTreePath(String treePath) {
      this.treePath = treePath;
      return this;
   }

   public boolean isPending() {
      return this.pending;
   }

   public IncidentEntity setPending(boolean pending) {
      this.pending = pending;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            IncidentEntity that = (IncidentEntity)o;
            return this.pending == that.pending && this.errorType == that.errorType && Objects.equals(this.errorMessage, that.errorMessage) && Objects.equals(this.errorMessageHash, that.errorMessageHash) && this.state == that.state && Objects.equals(this.flowNodeId, that.flowNodeId) && Objects.equals(this.flowNodeInstanceKey, that.flowNodeInstanceKey) && Objects.equals(this.jobKey, that.jobKey) && Objects.equals(this.processInstanceKey, that.processInstanceKey) && Objects.equals(this.creationTime, that.creationTime) && Objects.equals(this.processDefinitionKey, that.processDefinitionKey) && Objects.equals(this.treePath, that.treePath);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.errorType, this.errorMessage, this.errorMessageHash, this.state, this.flowNodeId, this.flowNodeInstanceKey, this.jobKey, this.processInstanceKey, this.creationTime, this.processDefinitionKey, this.treePath, this.pending});
   }

   public String toString() {
      long var10000 = this.getKey();
      return "IncidentEntity{key=" + var10000 + ", errorType=" + this.errorType + ", errorMessage='" + this.errorMessage + "', errorMessageHash=" + this.errorMessageHash + ", state=" + this.state + ", flowNodeId='" + this.flowNodeId + "', flowNodeInstanceKey=" + this.flowNodeInstanceKey + ", jobKey=" + this.jobKey + ", processInstanceKey=" + this.processInstanceKey + ", creationTime=" + this.creationTime + ", processDefinitionKey=" + this.processDefinitionKey + ", treePath='" + this.treePath + "', pending=" + this.pending + "}";
   }
}
