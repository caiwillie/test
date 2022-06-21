package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.Metrics;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.OperationsManager;
import io.camunda.operate.webapp.es.writer.BatchOperationWriter;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public abstract class AbstractOperationHandler implements OperationHandler {
   private static final Logger logger = LoggerFactory.getLogger(AbstractOperationHandler.class);
   private static final List RETRY_STATUSES;
   @Autowired
   protected BatchOperationWriter batchOperationWriter;
   @Autowired
   private OperationsManager operationsManager;
   @Autowired
   protected OperateProperties operateProperties;
   @Autowired
   protected Metrics metrics;

   public void handle(OperationEntity operation) {
      try {
         this.handleWithException(operation);
      } catch (Exception var5) {
         Exception ex = var5;
         if (this.isExceptionRetriable(var5)) {
            logger.error(String.format("Unable to process operation with id %s. Reason: %s. Will be retried.", operation.getId(), var5.getMessage()), var5);
         } else {
            try {
               this.failOperation(operation, String.format("Unable to process operation: %s", ex.getMessage()));
            } catch (PersistenceException var4) {
            }

            logger.error(String.format("Unable to process operation with id %s. Reason: %s. Will NOT be retried.", operation.getId(), var5.getMessage()), var5);
         }
      }

   }

   private boolean isExceptionRetriable(Exception ex) {
      StatusRuntimeException cause = this.extractStatusRuntimeException(ex);
      return cause != null && RETRY_STATUSES.contains(cause.getStatus().getCode());
   }

   private StatusRuntimeException extractStatusRuntimeException(Throwable ex) {
      if (ex.getCause() != null) {
         return ex.getCause() instanceof StatusRuntimeException ? (StatusRuntimeException)ex.getCause() : this.extractStatusRuntimeException(ex.getCause());
      } else {
         return null;
      }
   }

   protected void recordCommandMetric(OperationEntity operation) {
      this.metrics.recordCounts("commands", 1L, new String[]{"status", operation.getState().name(), "type", operation.getType().name()});
   }

   protected void failOperation(OperationEntity operation, String errorMsg) throws PersistenceException {
      if (this.isLocked(operation)) {
         operation.setState(OperationState.FAILED);
         operation.setLockExpirationTime((OffsetDateTime)null);
         operation.setLockOwner((String)null);
         operation.setErrorMessage(StringUtils.trimWhitespace(errorMsg));
         if (operation.getBatchOperationId() != null) {
            this.operationsManager.updateFinishedInBatchOperation(operation.getBatchOperationId());
         }

         this.batchOperationWriter.updateOperation(operation);
         logger.debug("Operation {} failed with message: {} ", operation.getId(), operation.getErrorMessage());
      }

      this.recordCommandMetric(operation);
   }

   private boolean isLocked(OperationEntity operation) {
      return operation.getState().equals(OperationState.LOCKED) && operation.getLockOwner().equals(this.operateProperties.getOperationExecutor().getWorkerId()) && this.getTypes().contains(operation.getType());
   }

   protected void markAsSent(OperationEntity operation) throws PersistenceException {
      this.markAsSent(operation, (Long)null);
   }

   protected void markAsSent(OperationEntity operation, Long zeebeCommandKey) throws PersistenceException {
      if (this.isLocked(operation)) {
         operation.setState(OperationState.SENT);
         operation.setLockExpirationTime((OffsetDateTime)null);
         operation.setLockOwner((String)null);
         operation.setZeebeCommandKey(zeebeCommandKey);
         this.batchOperationWriter.updateOperation(operation);
         logger.debug("Operation {} was sent to Zeebe", operation.getId());
      }

      this.recordCommandMetric(operation);
   }

   static {
      RETRY_STATUSES = Arrays.asList(Status.UNAVAILABLE.getCode(), Status.RESOURCE_EXHAUSTED.getCode(), Status.DEADLINE_EXCEEDED.getCode());
   }
}
