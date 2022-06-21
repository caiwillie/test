package io.camunda.operate.webapp.rest.dto.operation;

import io.camunda.operate.entities.OperationType;

public class CreateOperationRequestDto {
   private OperationType operationType;
   private String name;
   private String incidentId;
   private String variableScopeId;
   private String variableName;
   private String variableValue;

   public CreateOperationRequestDto() {
   }

   public CreateOperationRequestDto(OperationType operationType) {
      this.operationType = operationType;
   }

   public OperationType getOperationType() {
      return this.operationType;
   }

   public CreateOperationRequestDto setOperationType(OperationType operationType) {
      this.operationType = operationType;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getIncidentId() {
      return this.incidentId;
   }

   public void setIncidentId(String incidentId) {
      this.incidentId = incidentId;
   }

   public String getVariableScopeId() {
      return this.variableScopeId;
   }

   public void setVariableScopeId(String variableScopeId) {
      this.variableScopeId = variableScopeId;
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

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CreateOperationRequestDto that = (CreateOperationRequestDto)o;
         if (this.operationType != that.operationType) {
            return false;
         } else {
            if (this.name != null) {
               if (!this.name.equals(that.name)) {
                  return false;
               }
            } else if (that.name != null) {
               return false;
            }

            if (this.incidentId != null) {
               if (!this.incidentId.equals(that.incidentId)) {
                  return false;
               }
            } else if (that.incidentId != null) {
               return false;
            }

            label54: {
               if (this.variableScopeId != null) {
                  if (this.variableScopeId.equals(that.variableScopeId)) {
                     break label54;
                  }
               } else if (that.variableScopeId == null) {
                  break label54;
               }

               return false;
            }

            if (this.variableName != null) {
               if (this.variableName.equals(that.variableName)) {
                  return this.variableValue != null ? this.variableValue.equals(that.variableValue) : that.variableValue == null;
               }
            } else if (that.variableName == null) {
               return this.variableValue != null ? this.variableValue.equals(that.variableValue) : that.variableValue == null;
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.operationType != null ? this.operationType.hashCode() : 0;
      result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
      result = 31 * result + (this.incidentId != null ? this.incidentId.hashCode() : 0);
      result = 31 * result + (this.variableScopeId != null ? this.variableScopeId.hashCode() : 0);
      result = 31 * result + (this.variableName != null ? this.variableName.hashCode() : 0);
      result = 31 * result + (this.variableValue != null ? this.variableValue.hashCode() : 0);
      return result;
   }

   public String toString() {
      return "CreateOperationRequestDto{operationType=" + this.operationType + ", name='" + this.name + "', incidentId='" + this.incidentId + "', variableScopeId='" + this.variableScopeId + "', variableName='" + this.variableName + "', variableValue='" + this.variableValue + "'}";
   }
}
