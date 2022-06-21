package io.camunda.operate.webapp.rest.exception;

public class UserNotFoundException extends NotFoundException {
   private static final long serialVersionUID = 1L;

   public UserNotFoundException(String message) {
      super(message);
   }

   public UserNotFoundException(String message, Throwable cause) {
      super(message, cause);
   }
}
