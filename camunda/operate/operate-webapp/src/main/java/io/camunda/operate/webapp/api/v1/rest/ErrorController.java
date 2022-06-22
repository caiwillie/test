/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.webapp.api.v1.entities.Error
 *  io.camunda.operate.webapp.api.v1.exceptions.ClientException
 *  io.camunda.operate.webapp.api.v1.exceptions.ResourceNotFoundException
 *  io.camunda.operate.webapp.api.v1.exceptions.ServerException
 *  io.camunda.operate.webapp.api.v1.exceptions.ValidationException
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.ResponseStatus
 */
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

    @ResponseStatus(value=HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value={ClientException.class})
    public ResponseEntity<Error> handleInvalidRequest(ClientException exception) {
        this.logger.info(exception.getMessage(), (Throwable)exception);
        Error error = new Error().setType("Invalid request").setInstance(exception.getInstance()).setStatus(HttpStatus.BAD_REQUEST.value()).setMessage(exception.getMessage());
        return ResponseEntity.status((HttpStatus)HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(error);
    }

    @ResponseStatus(value=HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value={Exception.class})
    public ResponseEntity<Error> handleException(Exception exception) {
        return this.handleInvalidRequest(new ClientException(this.getOnlyDetailMessage(exception), (Throwable)exception));
    }

    private String getOnlyDetailMessage(Exception exception) {
        return StringUtils.substringBefore(exception.getMessage(), "; nested exception is");
    }

    @ResponseStatus(value=HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value={ValidationException.class})
    public ResponseEntity<Error> handleInvalidRequest(ValidationException exception) {
        this.logger.info(exception.getMessage(), (Throwable)exception);
        Error error = new Error().setType("Data invalid").setInstance(exception.getInstance()).setStatus(HttpStatus.BAD_REQUEST.value()).setMessage(exception.getMessage());
        return ResponseEntity.status((HttpStatus)HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(error);
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND)
    @ExceptionHandler(value={ResourceNotFoundException.class})
    public ResponseEntity<Error> handleNotFound(ResourceNotFoundException exception) {
        this.logger.info(exception.getMessage(), (Throwable)exception);
        Error error = new Error().setType("Requested resource not found").setInstance(exception.getInstance()).setStatus(HttpStatus.NOT_FOUND.value()).setMessage(exception.getMessage());
        return ResponseEntity.status((HttpStatus)HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(error);
    }

    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value={ServerException.class})
    public ResponseEntity<Error> handleServerException(ServerException exception) {
        this.logger.error(exception.getMessage(), (Throwable)exception);
        Error error = new Error().setType("API application error").setInstance(exception.getInstance()).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()).setMessage(exception.getMessage());
        return ResponseEntity.status((HttpStatus)HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(error);
    }
}
