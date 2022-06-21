package io.camunda.operate.webapp.api.v1.exceptions;

public class ClientException extends APIException {
   public static final String TYPE = "Invalid request";

   public ClientException(String message) {
      super(message);
   }

   public ClientException(String message, Throwable cause) {
      super(message, cause);
   }
}
