package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.definition.DecisionDefinitionEntity;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Decision object")
public class DecisionDto implements CreatableFromEntity {
   @ApiModelProperty("Unique id of the decision, must be used when filtering instances by decision ids.")
   private String id;
   private String name;
   private int version;
   private String decisionId;

   public String getId() {
      return this.id;
   }

   public DecisionDto setId(String id) {
      this.id = id;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public DecisionDto setName(String name) {
      this.name = name;
      return this;
   }

   public int getVersion() {
      return this.version;
   }

   public DecisionDto setVersion(int version) {
      this.version = version;
      return this;
   }

   public String getDecisionId() {
      return this.decisionId;
   }

   public DecisionDto setDecisionId(String decisionId) {
      this.decisionId = decisionId;
      return this;
   }

   public DecisionDto fillFrom(DecisionDefinitionEntity decisionEntity) {
      return this.setId(decisionEntity.getId()).setDecisionId(decisionEntity.getDecisionId()).setName(decisionEntity.getName()).setVersion(decisionEntity.getVersion());
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionDto that = (DecisionDto)o;
         if (this.version != that.version) {
            return false;
         } else {
            label44: {
               if (this.id != null) {
                  if (this.id.equals(that.id)) {
                     break label44;
                  }
               } else if (that.id == null) {
                  break label44;
               }

               return false;
            }

            if (this.name != null) {
               if (!this.name.equals(that.name)) {
                  return false;
               }
            } else if (that.name != null) {
               return false;
            }

            return this.decisionId != null ? this.decisionId.equals(that.decisionId) : that.decisionId == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.id != null ? this.id.hashCode() : 0;
      result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
      result = 31 * result + this.version;
      result = 31 * result + (this.decisionId != null ? this.decisionId.hashCode() : 0);
      return result;
   }
}
