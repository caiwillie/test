/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.OperationEntity
 *  io.camunda.operate.entities.OperationType
 *  io.camunda.operate.exceptions.PersistenceException
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.util.ThreadUtil
 *  io.camunda.operate.webapp.es.writer.BatchOperationWriter
 *  io.camunda.operate.webapp.zeebe.operation.ExecutionFinishedListener
 *  io.camunda.operate.webapp.zeebe.operation.OperationCommand
 *  io.camunda.operate.webapp.zeebe.operation.OperationExecutor$BlockCallerUntilExecutorHasCapacity
 *  io.camunda.operate.webapp.zeebe.operation.OperationHandler
 *  javax.annotation.PreDestroy
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.ThreadUtil;
import io.camunda.operate.webapp.es.writer.BatchOperationWriter;
import io.camunda.operate.webapp.zeebe.operation.ExecutionFinishedListener;
import io.camunda.operate.webapp.zeebe.operation.OperationCommand;
import io.camunda.operate.webapp.zeebe.operation.OperationExecutor;
import io.camunda.operate.webapp.zeebe.operation.OperationHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class OperationExecutor extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(OperationExecutor.class);
    private boolean shutdown = false;
    @Autowired
    private List<OperationHandler> handlers;
    @Autowired
    private BatchOperationWriter batchOperationWriter;
    @Autowired
    private OperateProperties operateProperties;
    @Autowired
    @Qualifier(value="operationsThreadPoolExecutor")
    private ThreadPoolTaskExecutor operationsTaskExecutor;
    private List<ExecutionFinishedListener> listeners = new ArrayList<ExecutionFinishedListener>();

    public void startExecuting() {
        if (!this.operateProperties.getOperationExecutor().isExecutorEnabled()) return;
        this.start();
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutdown OperationExecutor");
        this.shutdown = true;
    }

    @Override
    public void run() {
        while (!this.shutdown) {
            try {
                List<Future<?>> operations = this.executeOneBatch();
                if (operations.size() != 0) continue;
                this.notifyExecutionFinishedListeners();
                ThreadUtil.sleepFor((long)2000L);
            }
            catch (Exception ex) {
                logger.error("Something went wrong, while executing operations batch. Will be retried.", ex);
                ThreadUtil.sleepFor((long)2000L);
            }
        }
    }

    public List<Future<?>> executeOneBatch() throws PersistenceException {
        ArrayList futures = new ArrayList();
        List lockedOperations = this.batchOperationWriter.lockBatch();
        Iterator iterator = lockedOperations.iterator();
        while (iterator.hasNext()) {
            OperationEntity operation = (OperationEntity)iterator.next();
            OperationHandler handler = this.getOperationHandlers().get(operation.getType());
            if (handler == null) {
                logger.info("Operation {} on worflowInstanceId {} won't be processed, as no suitable handler was found.", (Object)operation.getType(), (Object)operation.getProcessInstanceKey());
                continue;
            }
            OperationCommand operationCommand = new OperationCommand(operation, handler);
            futures.add(this.operationsTaskExecutor.submit((Runnable)operationCommand));
        }
        return futures;
    }

    @Bean
    public Map<OperationType, OperationHandler> getOperationHandlers() {
        HashMap<OperationType, OperationHandler> handlerMap = new HashMap<OperationType, OperationHandler>();
        Iterator<OperationHandler> iterator = this.handlers.iterator();
        while (iterator.hasNext()) {
            OperationHandler handler = iterator.next();
            handler.getTypes().forEach(t -> handlerMap.put((OperationType)t, handler));
        }
        return handlerMap;
    }

    @Bean(value={"operationsThreadPoolExecutor"})
    public ThreadPoolTaskExecutor getOperationsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.operateProperties.getOperationExecutor().getThreadsCount());
        executor.setMaxPoolSize(this.operateProperties.getOperationExecutor().getThreadsCount());
        executor.setQueueCapacity(this.operateProperties.getOperationExecutor().getQueueSize());
        executor.setRejectedExecutionHandler((RejectedExecutionHandler)new BlockCallerUntilExecutorHasCapacity());
        executor.setThreadNamePrefix("operation_executor_");
        executor.initialize();
        return executor;
    }

    public void registerListener(ExecutionFinishedListener listener) {
        this.listeners.add(listener);
    }

    private void notifyExecutionFinishedListeners() {
        Iterator<ExecutionFinishedListener> iterator = this.listeners.iterator();
        while (iterator.hasNext()) {
            ExecutionFinishedListener listener = iterator.next();
            listener.onExecutionFinished();
        }
    }

    private class BlockCallerUntilExecutorHasCapacity implements RejectedExecutionHandler {
        private BlockCallerUntilExecutorHasCapacity() {
        }

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            if (executor.isShutdown()) return;
            try {
                executor.getQueue().put(runnable);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


}
