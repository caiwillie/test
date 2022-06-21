package io.camunda.operate.exceptions;

public class OperateRuntimeException extends RuntimeException {
   public OperateRuntimeException() {
   }

   public OperateRuntimeException(String message) {
      super(message);
   }

   public OperateRuntimeException(String message, Throwable cause) {
      super(message, cause);
   }

   public OperateRuntimeException(Throwable cause) {
      super(cause);
   }
}
