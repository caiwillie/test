package io.camunda.operate.webapp.rest.dto;

import java.util.Objects;

public class ProcessInstanceReferenceDto {
   private String instanceId;
   private String processDefinitionId;
   private String processDefinitionName;

   public String getInstanceId() {
      return this.instanceId;
   }

   public ProcessInstanceReferenceDto setInstanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
   }

   public String getProcessDefinitionId() {
      return this.processDefinitionId;
   }

   public ProcessInstanceReferenceDto setProcessDefinitionId(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
      return this;
   }

   public String getProcessDefinitionName() {
      return this.processDefinitionName;
   }

   public ProcessInstanceReferenceDto setProcessDefinitionName(String processDefinitionName) {
      this.processDefinitionName = processDefinitionName;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ProcessInstanceReferenceDto that = (ProcessInstanceReferenceDto)o;
         return Objects.equals(this.instanceId, that.instanceId) && Objects.equals(this.processDefinitionId, that.processDefinitionId) && Objects.equals(this.processDefinitionName, that.processDefinitionName);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.instanceId, this.processDefinitionId, this.processDefinitionName});
   }
}
