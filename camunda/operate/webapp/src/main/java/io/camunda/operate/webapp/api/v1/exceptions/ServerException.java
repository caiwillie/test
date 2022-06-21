package io.camunda.operate.webapp.api.v1.exceptions;

public class ServerException extends APIException {
   public static final String TYPE = "API application error";

   public ServerException(String message, Throwable cause) {
      super(message, cause);
   }

   public ServerException(String message) {
      super(message);
   }
}
