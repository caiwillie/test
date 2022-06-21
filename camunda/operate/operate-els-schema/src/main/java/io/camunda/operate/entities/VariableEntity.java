package io.camunda.operate.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.Objects;

public class VariableEntity extends OperateZeebeEntity {
   private String name;
   private String value;
   private String fullValue;
   private boolean isPreview;
   private Long scopeKey;
   private Long processInstanceKey;
   @JsonIgnore
   private Object[] sortValues;

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public String getFullValue() {
      return this.fullValue;
   }

   public VariableEntity setFullValue(String fullValue) {
      this.fullValue = fullValue;
      return this;
   }

   public boolean getIsPreview() {
      return this.isPreview;
   }

   public VariableEntity setIsPreview(boolean preview) {
      this.isPreview = preview;
      return this;
   }

   public Long getScopeKey() {
      return this.scopeKey;
   }

   public void setScopeKey(Long scopeKey) {
      this.scopeKey = scopeKey;
   }

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public void setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
   }

   public Object[] getSortValues() {
      return this.sortValues;
   }

   public VariableEntity setSortValues(Object[] sortValues) {
      this.sortValues = sortValues;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            VariableEntity that = (VariableEntity)o;
            return this.isPreview == that.isPreview && Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value) && Objects.equals(this.fullValue, that.fullValue) && Objects.equals(this.scopeKey, that.scopeKey) && Objects.equals(this.processInstanceKey, that.processInstanceKey) && Arrays.equals(this.sortValues, that.sortValues);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{super.hashCode(), this.name, this.value, this.fullValue, this.isPreview, this.scopeKey, this.processInstanceKey});
      result = 31 * result + Arrays.hashCode(this.sortValues);
      return result;
   }
}
