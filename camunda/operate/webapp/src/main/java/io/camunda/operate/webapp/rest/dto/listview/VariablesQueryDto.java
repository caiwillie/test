package io.camunda.operate.webapp.rest.dto.listview;

public class VariablesQueryDto {
   private String name;
   private String value;

   public VariablesQueryDto() {
   }

   public VariablesQueryDto(String variableName, String variableValue) {
      this.name = variableName;
      this.value = variableValue;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VariablesQueryDto that = (VariablesQueryDto)o;
         if (this.name != null) {
            if (this.name.equals(that.name)) {
               return this.value != null ? this.value.equals(that.value) : that.value == null;
            }
         } else if (that.name == null) {
            return this.value != null ? this.value.equals(that.value) : that.value == null;
         }

         return false;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.name != null ? this.name.hashCode() : 0;
      result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
      return result;
   }
}
