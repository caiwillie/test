package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.DecisionInstanceInputEntity;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import java.util.Objects;

public class DecisionInstanceInputDto implements CreatableFromEntity {
   private String id;
   private String name;
   private String value;

   public String getId() {
      return this.id;
   }

   public DecisionInstanceInputDto setId(String id) {
      this.id = id;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public DecisionInstanceInputDto setName(String name) {
      this.name = name;
      return this;
   }

   public String getValue() {
      return this.value;
   }

   public DecisionInstanceInputDto setValue(String value) {
      this.value = value;
      return this;
   }

   public DecisionInstanceInputDto fillFrom(DecisionInstanceInputEntity inputEntity) {
      return this.setId(inputEntity.getId()).setName(inputEntity.getName()).setValue(inputEntity.getValue());
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionInstanceInputDto that = (DecisionInstanceInputDto)o;
         return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.name, this.value});
   }
}
