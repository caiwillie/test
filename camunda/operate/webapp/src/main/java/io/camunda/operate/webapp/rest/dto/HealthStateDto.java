package io.camunda.operate.webapp.rest.dto;

public class HealthStateDto {
   public static final String HEALTH_STATUS_OK = "OK";
   private String state;

   public HealthStateDto() {
   }

   public HealthStateDto(String state) {
      this.state = state;
   }

   public String getState() {
      return this.state;
   }

   public void setState(String state) {
      this.state = state;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         HealthStateDto that = (HealthStateDto)o;
         return this.state != null ? this.state.equals(that.state) : that.state == null;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.state != null ? this.state.hashCode() : 0;
   }
}
