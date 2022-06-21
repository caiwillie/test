package io.camunda.operate.entities.dmn.definition;

import io.camunda.operate.entities.OperateZeebeEntity;
import java.util.Objects;

public class DecisionRequirementsEntity extends OperateZeebeEntity {
   private String decisionRequirementsId;
   private String name;
   private int version;
   private String xml;
   private String resourceName;

   public String getDecisionRequirementsId() {
      return this.decisionRequirementsId;
   }

   public DecisionRequirementsEntity setDecisionRequirementsId(String decisionRequirementsId) {
      this.decisionRequirementsId = decisionRequirementsId;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public DecisionRequirementsEntity setName(String name) {
      this.name = name;
      return this;
   }

   public int getVersion() {
      return this.version;
   }

   public DecisionRequirementsEntity setVersion(int version) {
      this.version = version;
      return this;
   }

   public String getXml() {
      return this.xml;
   }

   public DecisionRequirementsEntity setXml(String xml) {
      this.xml = xml;
      return this;
   }

   public String getResourceName() {
      return this.resourceName;
   }

   public DecisionRequirementsEntity setResourceName(String resourceName) {
      this.resourceName = resourceName;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            DecisionRequirementsEntity that = (DecisionRequirementsEntity)o;
            return this.version == that.version && Objects.equals(this.decisionRequirementsId, that.decisionRequirementsId) && Objects.equals(this.name, that.name) && Objects.equals(this.xml, that.xml) && Objects.equals(this.resourceName, that.resourceName);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{super.hashCode(), this.decisionRequirementsId, this.name, this.version, this.xml, this.resourceName});
   }
}
