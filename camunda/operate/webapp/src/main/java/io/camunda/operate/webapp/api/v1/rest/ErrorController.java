package io.camunda.operate.webapp.api.v1.rest;

import io.camunda.operate.webapp.api.v1.entities.Error;
import io.camunda.operate.webapp.api.v1.exceptions.ClientException;
import io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException;
import io.camunda.operate.webapp.api.v1.exceptions.ServerException;
import io.camunda.operate.webapp.api.v1.exceptions.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class ErrorController {
   protected final Logger logger = LoggerFactory.getLogger(this.getClass());

   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ExceptionHandler({ClientException.class})
   public ResponseEntity handleInvalidRequest(ClientException exception) {
      this.logger.info(exception.getMessage(), exception);
      Error error = (new Error()).setType("Invalid request").setInstance(exception.getInstance()).setStatus(HttpStatus.BAD_REQUEST.value()).setMessage(exception.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(error);
   }

   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ExceptionHandler({Exception.class})
   public ResponseEntity handleException(Exception exception) {
      return this.handleInvalidRequest(new ClientException(this.getOnlyDetailMessage(exception), exception));
   }

   private String getOnlyDetailMessage(Exception exception) {
      return StringUtils.substringBefore(exception.getMessage(), "; nested exception is");
   }

   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ExceptionHandler({ValidationException.class})
   public ResponseEntity handleInvalidRequest(ValidationException exception) {
      this.logger.info(exception.getMessage(), exception);
      Error error = (new Error()).setType("Data invalid").setInstance(exception.getInstance()).setStatus(HttpStatus.BAD_REQUEST.value()).setMessage(exception.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(error);
   }

   @ResponseStatus(HttpStatus.NOT_FOUND)
   @ExceptionHandler({ResourceNotFoundException.class})
   public ResponseEntity handleNotFound(ResourceNotFoundException exception) {
      this.logger.info(exception.getMessage(), exception);
      Error error = (new Error()).setType("Requested resource not found").setInstance(exception.getInstance()).setStatus(HttpStatus.NOT_FOUND.value()).setMessage(exception.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(error);
   }

   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   @ExceptionHandler({ServerException.class})
   public ResponseEntity handleServerException(ServerException exception) {
      this.logger.error(exception.getMessage(), exception);
      Error error = (new Error()).setType("API application error").setInstance(exception.getInstance()).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()).setMessage(exception.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(error);
   }
}
