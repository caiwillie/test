package io.camunda.operate.entities.listview;

import io.camunda.operate.entities.OperateZeebeEntity;

public class VariableForListViewEntity extends OperateZeebeEntity {
   private Long processInstanceKey;
   private Long scopeKey;
   private String varName;
   private String varValue;
   private ListViewJoinRelation joinRelation = new ListViewJoinRelation("variable");

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public static String getIdBy(long scopeKey, String name) {
      return String.format("%d-%s", scopeKey, name);
   }

   public void setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
   }

   public Long getScopeKey() {
      return this.scopeKey;
   }

   public void setScopeKey(Long scopeKey) {
      this.scopeKey = scopeKey;
   }

   public String getVarName() {
      return this.varName;
   }

   public void setVarName(String varName) {
      this.varName = varName;
   }

   public String getVarValue() {
      return this.varValue;
   }

   public void setVarValue(String varValue) {
      this.varValue = varValue;
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
            VariableForListViewEntity that = (VariableForListViewEntity)o;
            if (this.processInstanceKey != null) {
               if (!this.processInstanceKey.equals(that.processInstanceKey)) {
                  return false;
               }
            } else if (that.processInstanceKey != null) {
               return false;
            }

            if (this.scopeKey != null) {
               if (!this.scopeKey.equals(that.scopeKey)) {
                  return false;
               }
            } else if (that.scopeKey != null) {
               return false;
            }

            label54: {
               if (this.varName != null) {
                  if (this.varName.equals(that.varName)) {
                     break label54;
                  }
               } else if (that.varName == null) {
                  break label54;
               }

               return false;
            }

            if (this.varValue != null) {
               if (this.varValue.equals(that.varValue)) {
                  return this.joinRelation != null ? this.joinRelation.equals(that.joinRelation) : that.joinRelation == null;
               }
            } else if (that.varValue == null) {
               return this.joinRelation != null ? this.joinRelation.equals(that.joinRelation) : that.joinRelation == null;
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
      result = 31 * result + (this.scopeKey != null ? this.scopeKey.hashCode() : 0);
      result = 31 * result + (this.varName != null ? this.varName.hashCode() : 0);
      result = 31 * result + (this.varValue != null ? this.varValue.hashCode() : 0);
      result = 31 * result + (this.joinRelation != null ? this.joinRelation.hashCode() : 0);
      return result;
   }
}
