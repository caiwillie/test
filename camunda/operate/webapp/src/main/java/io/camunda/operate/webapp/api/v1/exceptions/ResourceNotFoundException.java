package io.camunda.operate.webapp.api.v1.exceptions;

public class ResourceNotFoundException extends APIException {
   public static final String TYPE = "Requested resource not found";

   public ResourceNotFoundException(String message) {
      super(message);
   }
}
