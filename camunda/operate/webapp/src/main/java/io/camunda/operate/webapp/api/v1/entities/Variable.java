package io.camunda.operate.webapp.api.v1.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class Variable {
   public static final String KEY = "key";
   public static final String PROCESS_INSTANCE_KEY = "processInstanceKey";
   public static final String SCOPE_KEY = "scopeKey";
   public static final String NAME = "name";
   public static final String VALUE = "value";
   public static final String FULL_VALUE = "fullValue";
   public static final String TRUNCATED = "isPreview";
   private Long key;
   private Long processInstanceKey;
   private Long scopeKey;
   private String name;
   private String value;
   private Boolean truncated;

   public Long getKey() {
      return this.key;
   }

   public Variable setKey(Long key) {
      this.key = key;
      return this;
   }

   public Long getProcessInstanceKey() {
      return this.processInstanceKey;
   }

   public Variable setProcessInstanceKey(Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
      return this;
   }

   public Long getScopeKey() {
      return this.scopeKey;
   }

   public Variable setScopeKey(Long scopeKey) {
      this.scopeKey = scopeKey;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public Variable setName(String name) {
      this.name = name;
      return this;
   }

   public String getValue() {
      return this.value;
   }

   public Variable setValue(String value) {
      this.value = value;
      return this;
   }

   public Boolean getTruncated() {
      return this.truncated;
   }

   public Variable setTruncated(Boolean truncated) {
      this.truncated = truncated;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Variable variable = (Variable)o;
         return Objects.equals(this.key, variable.key) && Objects.equals(this.processInstanceKey, variable.processInstanceKey) && Objects.equals(this.scopeKey, variable.scopeKey) && Objects.equals(this.name, variable.name) && Objects.equals(this.value, variable.value) && Objects.equals(this.truncated, variable.truncated);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.key, this.processInstanceKey, this.scopeKey, this.name, this.value, this.truncated});
   }

   public String toString() {
      return "Variable{key=" + this.key + ", processInstanceKey=" + this.processInstanceKey + ", scopeKey=" + this.scopeKey + ", name='" + this.name + "', value='" + this.value + "', truncated=" + this.truncated + "}";
   }
}
