/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.Metrics
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationState
 *  io.camunda.operate.exceptions.PersistenceException
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.util.OperationsManager
 *  io.camunda.operate.webapp.es.writer.BatchOperationWriter
 *  io.camunda.operate.webapp.zeebe.operation.OperationHandler
 *  io.grpc.Status
 *  io.grpc.Status$Code
 *  io.grpc.StatusRuntimeException
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.util.StringUtils
 */
package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.Metrics;
import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationState;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.OperationsManager;
import io.camunda.operate.webapp.es.writer.BatchOperationWriter;
import io.camunda.operate.webapp.zeebe.operation.OperationHandler;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public abstract class AbstractOperationHandler
implements OperationHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractOperationHandler.class);
    private static final List<Status.Code> RETRY_STATUSES = Arrays.asList(Status.UNAVAILABLE.getCode(), Status.RESOURCE_EXHAUSTED.getCode(), Status.DEADLINE_EXCEEDED.getCode());
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
        }
        catch (Exception ex) {
            if (this.isExceptionRetriable(ex)) {
                logger.error(String.format("Unable to process operation with id %s. Reason: %s. Will be retried.", operation.getId(), ex.getMessage()), ex);
            }
            try {
                this.failOperation(operation, String.format("Unable to process operation: %s", ex.getMessage()));
            }
            catch (PersistenceException persistenceException) {
                // empty catch block
            }
            logger.error(String.format("Unable to process operation with id %s. Reason: %s. Will NOT be retried.", operation.getId(), ex.getMessage()), ex);
        }
    }

    private boolean isExceptionRetriable(Exception ex) {
        StatusRuntimeException cause = this.extractStatusRuntimeException(ex);
        return cause != null && RETRY_STATUSES.contains(cause.getStatus().getCode());
    }

    private StatusRuntimeException extractStatusRuntimeException(Throwable ex) {
        if (ex.getCause() == null) return null;
        if (!(ex.getCause() instanceof StatusRuntimeException)) return this.extractStatusRuntimeException(ex.getCause());
        return (StatusRuntimeException)ex.getCause();
    }

    protected void recordCommandMetric(OperationEntity operation) {
        this.metrics.recordCounts("commands", 1L, new String[]{"status", operation.getState().name(), "type", operation.getType().name()});
    }

    protected void failOperation(OperationEntity operation, String errorMsg) throws PersistenceException {
        if (this.isLocked(operation)) {
            operation.setState(OperationState.FAILED);
            operation.setLockExpirationTime(null);
            operation.setLockOwner(null);
            operation.setErrorMessage(StringUtils.trimWhitespace((String)errorMsg));
            if (operation.getBatchOperationId() != null) {
                this.operationsManager.updateFinishedInBatchOperation(operation.getBatchOperationId());
            }
            this.batchOperationWriter.updateOperation(operation);
            logger.debug("Operation {} failed with message: {} ", (Object)operation.getId(), (Object)operation.getErrorMessage());
        }
        this.recordCommandMetric(operation);
    }

    private boolean isLocked(OperationEntity operation) {
        return operation.getState().equals((Object)OperationState.LOCKED) && operation.getLockOwner().equals(this.operateProperties.getOperationExecutor().getWorkerId()) && this.getTypes().contains(operation.getType());
    }

    protected void markAsSent(OperationEntity operation) throws PersistenceException {
        this.markAsSent(operation, null);
    }

    protected void markAsSent(OperationEntity operation, Long zeebeCommandKey) throws PersistenceException {
        if (this.isLocked(operation)) {
            operation.setState(OperationState.SENT);
            operation.setLockExpirationTime(null);
            operation.setLockOwner(null);
            operation.setZeebeCommandKey(zeebeCommandKey);
            this.batchOperationWriter.updateOperation(operation);
            logger.debug("Operation {} was sent to Zeebe", (Object)operation.getId());
        }
        this.recordCommandMetric(operation);
    }
}
