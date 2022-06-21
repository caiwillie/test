package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.SequenceFlowEntity;
import io.camunda.operate.util.ConversionUtils;

public class SequenceFlowDto implements CreatableFromEntity {
   private String processInstanceId;
   private String activityId;

   public String getProcessInstanceId() {
      return this.processInstanceId;
   }

   public SequenceFlowDto setProcessInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
      return this;
   }

   public String getActivityId() {
      return this.activityId;
   }

   public SequenceFlowDto setActivityId(String activityId) {
      this.activityId = activityId;
      return this;
   }

   public SequenceFlowDto fillFrom(SequenceFlowEntity entity) {
      return this.setProcessInstanceId(ConversionUtils.toStringOrNull(entity.getProcessInstanceKey())).setActivityId(entity.getActivityId());
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SequenceFlowDto that = (SequenceFlowDto)o;
         if (this.processInstanceId != null) {
            if (this.processInstanceId.equals(that.processInstanceId)) {
               return this.activityId != null ? this.activityId.equals(that.activityId) : that.activityId == null;
            }
         } else if (that.processInstanceId == null) {
            return this.activityId != null ? this.activityId.equals(that.activityId) : that.activityId == null;
         }

         return false;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.processInstanceId != null ? this.processInstanceId.hashCode() : 0;
      result = 31 * result + (this.activityId != null ? this.activityId.hashCode() : 0);
      return result;
   }
}
