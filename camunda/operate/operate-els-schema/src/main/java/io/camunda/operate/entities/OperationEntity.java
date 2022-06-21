package io.camunda.operate.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public class OperationEntity extends OperateEntity {
   private Long processInstanceKey;
   private Long incidentKey;
   private Long scopeKey;
   private String variableName;
   private String variableValue;
   private OperationType type;
   private OffsetDateTime lockExpirationTime;
   private String lockOwner;
   private OperationState state;
   private String errorMessage;
   private String batchOperationId;
   private Long zeebeCommandKey;
   private String username;

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public void setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
   }

   public Long getIncidentKey() {
      return this.incidentKey;
   }

   public void setIncidentKey(Long incidentKey) {
      this.incidentKey = incidentKey;
   }

   public Long getScopeKey() {
      return this.scopeKey;
   }

   public void setScopeKey(Long scopeKey) {
      this.scopeKey = scopeKey;
   }

   public String getVariableName() {
      return this.variableName;
   }

   public void setVariableName(String variableName) {
      this.variableName = variableName;
   }

   public String getVariableValue() {
      return this.variableValue;
   }

   public void setVariableValue(String variableValue) {
      this.variableValue = variableValue;
   }

   public OperationType getType() {
      return this.type;
   }

   public void setType(OperationType type) {
      this.type = type;
   }

   public Long getZeebeCommandKey() {
      return this.zeebeCommandKey;
   }

   public void setZeebeCommandKey(Long zeebeCommandKey) {
      this.zeebeCommandKey = zeebeCommandKey;
   }

   public OperationState getState() {
      return this.state;
   }

   public void setState(OperationState state) {
      this.state = state;
   }

   public OffsetDateTime getLockExpirationTime() {
      return this.lockExpirationTime;
   }

   public void setLockExpirationTime(OffsetDateTime lockExpirationTime) {
      this.lockExpirationTime = lockExpirationTime;
   }

   public String getLockOwner() {
      return this.lockOwner;
   }

   public void setLockOwner(String lockOwner) {
      this.lockOwner = lockOwner;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
   }

   public String getBatchOperationId() {
      return this.batchOperationId;
   }

   public void setBatchOperationId(String batchOperationId) {
      this.batchOperationId = batchOperationId;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public void generateId() {
      this.setId(UUID.randomUUID().toString());
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            OperationEntity that = (OperationEntity)o;
            if (this.processInstanceKey != null) {
               if (!this.processInstanceKey.equals(that.processInstanceKey)) {
                  return false;
               }
            } else if (that.processInstanceKey != null) {
               return false;
            }

            if (this.incidentKey != null) {
               if (!this.incidentKey.equals(that.incidentKey)) {
                  return false;
               }
            } else if (that.incidentKey != null) {
               return false;
            }

            label134: {
               if (this.scopeKey != null) {
                  if (this.scopeKey.equals(that.scopeKey)) {
                     break label134;
                  }
               } else if (that.scopeKey == null) {
                  break label134;
               }

               return false;
            }

            label127: {
               if (this.variableName != null) {
                  if (this.variableName.equals(that.variableName)) {
                     break label127;
                  }
               } else if (that.variableName == null) {
                  break label127;
               }

               return false;
            }

            if (this.variableValue != null) {
               if (!this.variableValue.equals(that.variableValue)) {
                  return false;
               }
            } else if (that.variableValue != null) {
               return false;
            }

            if (this.type != that.type) {
               return false;
            } else {
               if (this.lockExpirationTime != null) {
                  if (!this.lockExpirationTime.equals(that.lockExpirationTime)) {
                     return false;
                  }
               } else if (that.lockExpirationTime != null) {
                  return false;
               }

               label105: {
                  if (this.lockOwner != null) {
                     if (this.lockOwner.equals(that.lockOwner)) {
                        break label105;
                     }
                  } else if (that.lockOwner == null) {
                     break label105;
                  }

                  return false;
               }

               if (this.state != that.state) {
                  return false;
               } else {
                  if (this.errorMessage != null) {
                     if (!this.errorMessage.equals(that.errorMessage)) {
                        return false;
                     }
                  } else if (that.errorMessage != null) {
                     return false;
                  }

                  label90: {
                     if (this.batchOperationId != null) {
                        if (this.batchOperationId.equals(that.batchOperationId)) {
                           break label90;
                        }
                     } else if (that.batchOperationId == null) {
                        break label90;
                     }

                     return false;
                  }

                  if (this.zeebeCommandKey != null) {
                     if (this.zeebeCommandKey.equals(that.zeebeCommandKey)) {
                        return this.username != null ? this.username.equals(that.username) : that.username == null;
                     }
                  } else if (that.zeebeCommandKey == null) {
                     return this.username != null ? this.username.equals(that.username) : that.username == null;
                  }

                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.processInstanceKey != null ? this.processInstanceKey.hashCode() : 0);
      result = 31 * result + (this.incidentKey != null ? this.incidentKey.hashCode() : 0);
      result = 31 * result + (this.scopeKey != null ? this.scopeKey.hashCode() : 0);
      result = 31 * result + (this.variableName != null ? this.variableName.hashCode() : 0);
      result = 31 * result + (this.variableValue != null ? this.variableValue.hashCode() : 0);
      result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
      result = 31 * result + (this.lockExpirationTime != null ? this.lockExpirationTime.hashCode() : 0);
      result = 31 * result + (this.lockOwner != null ? this.lockOwner.hashCode() : 0);
      result = 31 * result + (this.state != null ? this.state.hashCode() : 0);
      result = 31 * result + (this.errorMessage != null ? this.errorMessage.hashCode() : 0);
      result = 31 * result + (this.batchOperationId != null ? this.batchOperationId.hashCode() : 0);
      result = 31 * result + (this.zeebeCommandKey != null ? this.zeebeCommandKey.hashCode() : 0);
      result = 31 * result + (this.username != null ? this.username.hashCode() : 0);
      return result;
   }

   public String toString() {
      return "OperationEntity{processInstanceKey=" + this.processInstanceKey + ", incidentKey=" + this.incidentKey + ", scopeKey=" + this.scopeKey + ", variableName='" + this.variableName + "', variableValue='" + this.variableValue + "', type=" + this.type + ", lockExpirationTime=" + this.lockExpirationTime + ", lockOwner='" + this.lockOwner + "', state=" + this.state + ", errorMessage='" + this.errorMessage + "', batchOperationId='" + this.batchOperationId + "', zeebeCommandKey=" + this.zeebeCommandKey + "}";
   }
}
