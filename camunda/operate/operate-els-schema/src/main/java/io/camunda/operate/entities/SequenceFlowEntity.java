package io.camunda.operate.entities;

public class SequenceFlowEntity extends OperateEntity {
   private Long processInstanceKey;
   private String activityId;

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public SequenceFlowEntity setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
      return this;
   }

   public String getActivityId() {
      return this.activityId;
   }

   public SequenceFlowEntity setActivityId(String activityId) {
      this.activityId = activityId;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            SequenceFlowEntity that = (SequenceFlowEntity)o;
            if (this.processInstanceKey != null) {
               if (this.processInstanceKey.equals(that.processInstanceKey)) {
                  return this.activityId != null ? this.activityId.equals(that.activityId) : that.activityId == null;
               }
            } else if (that.processInstanceKey == null) {
               return this.activityId != null ? this.activityId.equals(that.activityId) : that.activityId == null;
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.processInstanceKey != null ? this.processInstanceKey.hashCode() : 0);
      result = 31 * result + (this.activityId != null ? this.activityId.hashCode() : 0);
      return result;
   }
}
