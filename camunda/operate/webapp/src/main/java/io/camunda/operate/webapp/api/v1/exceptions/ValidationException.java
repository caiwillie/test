package io.camunda.operate.webapp.api.v1.exceptions;

public class ValidationException extends APIException {
   public static final String TYPE = "Data invalid";

   public ValidationException(String message) {
      super(message);
   }

   public ValidationException(String message, Throwable cause) {
      super(message, cause);
   }
}
