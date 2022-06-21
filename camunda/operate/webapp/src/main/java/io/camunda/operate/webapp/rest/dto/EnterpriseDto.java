package io.camunda.operate.webapp.rest.dto;

import java.util.Objects;

public class EnterpriseDto {
   private final boolean enterprise;

   public EnterpriseDto(boolean enterprise) {
      this.enterprise = enterprise;
   }

   public boolean isEnterprise() {
      return this.enterprise;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EnterpriseDto that = (EnterpriseDto)o;
         return this.enterprise == that.enterprise;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.enterprise});
   }

   public String toString() {
      return "EnterpriseDto{enterprise=" + this.enterprise + "}";
   }
}
