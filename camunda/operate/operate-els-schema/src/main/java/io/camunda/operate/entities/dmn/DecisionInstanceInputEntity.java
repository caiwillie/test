package io.camunda.operate.entities.dmn;

import java.util.Objects;

public class DecisionInstanceInputEntity {
   private String id;
   private String name;
   private String value;

   public String getId() {
      return this.id;
   }

   public DecisionInstanceInputEntity setId(String id) {
      this.id = id;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public DecisionInstanceInputEntity setName(String name) {
      this.name = name;
      return this;
   }

   public String getValue() {
      return this.value;
   }

   public DecisionInstanceInputEntity setValue(String value) {
      this.value = value;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecisionInstanceInputEntity that = (DecisionInstanceInputEntity)o;
         return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.name, this.value});
   }
}
