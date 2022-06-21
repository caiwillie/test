package io.camunda.operate.entities.dmn;

import java.util.Objects;

public class DecisionInstanceOutputEntity {
   private String id;
   private String name;
   private String value;
   private String ruleId;
   private int ruleIndex;

   public String getId() {
      return this.id;
   }

   public DecisionInstanceOutputEntity setId(String id) {
      this.id = id;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public DecisionInstanceOutputEntity setName(String name) {
      this.name = name;
      return this;
   }

   public String getValue() {
      return this.value;
   }

   public DecisionInstanceOutputEntity setValue(String value) {
      this.value = value;
      return this;
   }

   public String getRuleId() {
      return this.ruleId;
   }

   public DecisionInstanceOutputEntity setRuleId(String ruleId) {
      this.ruleId = ruleId;
      return this;
   }

   public int getRuleIndex() {
      return this.ruleIndex;
   }

   public DecisionInstanceOutputEntity setRuleIndex(int ruleIndex) {
      this.ruleIndex = ruleIndex;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionInstanceOutputEntity that = (DecisionInstanceOutputEntity)o;
         return this.ruleIndex == that.ruleIndex && Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value) && Objects.equals(this.ruleId, that.ruleId);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.name, this.value, this.ruleId, this.ruleIndex});
   }
}
