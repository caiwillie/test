package io.camunda.operate.webapp.api.v1.entities;

public class Error {
   private int status;
   private String message;
   private String instance;
   private String type;

   public String getType() {
      return this.type;
   }

   public Error setType(String type) {
      this.type = type;
      return this;
   }

   public int getStatus() {
      return this.status;
   }

   public Error setStatus(int status) {
      this.status = status;
      return this;
   }

   public String getInstance() {
      return this.instance;
   }

   public Error setInstance(String instance) {
      this.instance = instance;
      return this;
   }

   public String getMessage() {
      return this.message;
   }

   public Error setMessage(String message) {
      this.message = message;
      return this;
   }
}
