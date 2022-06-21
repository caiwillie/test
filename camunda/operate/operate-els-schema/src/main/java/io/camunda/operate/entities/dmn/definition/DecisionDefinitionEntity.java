package io.camunda.operate.entities.dmn.definition;

import io.camunda.operate.entities.OperateZeebeEntity;
import java.util.Objects;

public class DecisionDefinitionEntity extends OperateZeebeEntity {
   private String decisionId;
   private String name;
   private int version;
   private String decisionRequirementsId;
   private long decisionRequirementsKey;

   public String getDecisionId() {
      return this.decisionId;
   }

   public DecisionDefinitionEntity setDecisionId(String decisionId) {
      this.decisionId = decisionId;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public DecisionDefinitionEntity setName(String name) {
      this.name = name;
      return this;
   }

   public int getVersion() {
      return this.version;
   }

   public DecisionDefinitionEntity setVersion(int version) {
      this.version = version;
      return this;
   }

   public String getDecisionRequirementsId() {
      return this.decisionRequirementsId;
   }

   public DecisionDefinitionEntity setDecisionRequirementsId(String decisionRequirementsId) {
      this.decisionRequirementsId = decisionRequirementsId;
      return this;
   }

   public long getDecisionRequirementsKey() {
      return this.decisionRequirementsKey;
   }

   public DecisionDefinitionEntity setDecisionRequirementsKey(long decisionRequirementsKey) {
      this.decisionRequirementsKey = decisionRequirementsKey;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            DecisionDefinitionEntity that = (DecisionDefinitionEntity)o;
            return this.version == that.version && this.decisionRequirementsKey == that.decisionRequirementsKey && Objects.equals(this.decisionId, that.decisionId) && Objects.equals(this.name, that.name) && Objects.equals(this.decisionRequirementsId, that.decisionRequirementsId);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.decisionId, this.name, this.version, this.decisionRequirementsId, this.decisionRequirementsKey});
   }
}
