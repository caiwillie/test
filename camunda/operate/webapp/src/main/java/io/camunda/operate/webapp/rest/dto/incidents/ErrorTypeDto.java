package io.camunda.operate.webapp.rest.dto.incidents;

import io.camunda.operate.entities.ErrorType;
import java.util.Objects;

public class ErrorTypeDto implements Comparable {
   private String id;
   private String name;

   public String getId() {
      return this.id;
   }

   public ErrorTypeDto setId(String id) {
      this.id = id;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public ErrorTypeDto setName(String name) {
      this.name = name;
      return this;
   }

   public static ErrorTypeDto createFrom(ErrorType errorType) {
      return (new ErrorTypeDto()).setId(errorType.name()).setName(errorType.getTitle());
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ErrorTypeDto that = (ErrorTypeDto)o;
         return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.name});
   }

   public String toString() {
      return "ErrorTypeDto{id='" + this.id + "', name='" + this.name + "'}";
   }

   public int compareTo(ErrorTypeDto o) {
      return this.id != null ? this.id.compareTo(o.getId()) : 0;
   }
}
