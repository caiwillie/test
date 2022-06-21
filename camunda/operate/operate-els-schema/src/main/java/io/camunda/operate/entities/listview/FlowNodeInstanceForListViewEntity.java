package io.camunda.operate.entities.listview;

import io.camunda.operate.entities.FlowNodeState;
import io.camunda.operate.entities.FlowNodeType;
import io.camunda.operate.entities.OperateZeebeEntity;

public class FlowNodeInstanceForListViewEntity extends OperateZeebeEntity {
   private Long processInstanceKey;
   private String activityId;
   private FlowNodeState activityState;
   private FlowNodeType activityType;
   private Long incidentKey;
   private String errorMessage;
   private Long incidentJobKey;
   private boolean incident;
   private ListViewJoinRelation joinRelation = new ListViewJoinRelation("activity");

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public void setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
   }

   public String getActivityId() {
      return this.activityId;
   }

   public void setActivityId(String activityId) {
      this.activityId = activityId;
   }

   public FlowNodeState getActivityState() {
      return this.activityState;
   }

   public void setActivityState(FlowNodeState activityState) {
      this.activityState = activityState;
   }

   public FlowNodeType getActivityType() {
      return this.activityType;
   }

   public void setActivityType(FlowNodeType activityType) {
      this.activityType = activityType;
   }

   public Long getIncidentKey() {
      return this.incidentKey;
   }

   public void setIncidentKey(Long incidentKey) {
      this.incidentKey = incidentKey;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
   }

   public Long getIncidentJobKey() {
      return this.incidentJobKey;
   }

   public void setIncidentJobKey(Long incidentJobKey) {
      this.incidentJobKey = incidentJobKey;
   }

   public boolean isIncident() {
      return this.incident;
   }

   public FlowNodeInstanceForListViewEntity setIncident(boolean incident) {
      this.incident = incident;
      return this;
   }

   public ListViewJoinRelation getJoinRelation() {
      return this.joinRelation;
   }

   public void setJoinRelation(ListViewJoinRelation joinRelation) {
      this.joinRelation = joinRelation;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            FlowNodeInstanceForListViewEntity that;
            label88: {
               that = (FlowNodeInstanceForListViewEntity)o;
               if (this.processInstanceKey != null) {
                  if (this.processInstanceKey.equals(that.processInstanceKey)) {
                     break label88;
                  }
               } else if (that.processInstanceKey == null) {
                  break label88;
               }

               return false;
            }

            if (this.activityId != null) {
               if (!this.activityId.equals(that.activityId)) {
                  return false;
               }
            } else if (that.activityId != null) {
               return false;
            }

            if (this.activityState != that.activityState) {
               return false;
            } else if (this.activityType != that.activityType) {
               return false;
            } else {
               label71: {
                  if (this.incidentKey != null) {
                     if (this.incidentKey.equals(that.incidentKey)) {
                        break label71;
                     }
                  } else if (that.incidentKey == null) {
                     break label71;
                  }

                  return false;
               }

               label64: {
                  if (this.errorMessage != null) {
                     if (this.errorMessage.equals(that.errorMessage)) {
                        break label64;
                     }
                  } else if (that.errorMessage == null) {
                     break label64;
                  }

                  return false;
               }

               if (this.incidentJobKey != null) {
                  if (this.incidentJobKey.equals(that.incidentJobKey)) {
                     return this.joinRelation != null ? this.joinRelation.equals(that.joinRelation) : that.joinRelation == null;
                  }
               } else if (that.incidentJobKey == null) {
                  return this.joinRelation != null ? this.joinRelation.equals(that.joinRelation) : that.joinRelation == null;
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.processInstanceKey != null ? this.processInstanceKey.hashCode() : 0);
      result = 31 * result + (this.activityId != null ? this.activityId.hashCode() : 0);
      result = 31 * result + (this.activityState != null ? this.activityState.hashCode() : 0);
      result = 31 * result + (this.activityType != null ? this.activityType.hashCode() : 0);
      result = 31 * result + (this.incidentKey != null ? this.incidentKey.hashCode() : 0);
      result = 31 * result + (this.errorMessage != null ? this.errorMessage.hashCode() : 0);
      result = 31 * result + (this.incidentJobKey != null ? this.incidentJobKey.hashCode() : 0);
      result = 31 * result + (this.joinRelation != null ? this.joinRelation.hashCode() : 0);
      return result;
   }
}
