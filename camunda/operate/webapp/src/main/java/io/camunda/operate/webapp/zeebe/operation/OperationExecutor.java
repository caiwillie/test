package io.camunda.operate.webapp.zeebe.operation;

import io.camunda.operate.entities.OperationEntity;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.ThreadUtil;
import io.camunda.operate.webapp.es.writer.BatchOperationWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
   private List handlers;
   @Autowired
   private BatchOperationWriter batchOperationWriter;
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   @Qualifier("operationsThreadPoolExecutor")
   private ThreadPoolTaskExecutor operationsTaskExecutor;
   private List listeners = new ArrayList();

   public void startExecuting() {
      if (this.operateProperties.getOperationExecutor().isExecutorEnabled()) {
         this.start();
      }

   }

   @PreDestroy
   public void shutdown() {
      logger.info("Shutdown OperationExecutor");
      this.shutdown = true;
   }

   public void run() {
      while(!this.shutdown) {
         try {
            List operations = this.executeOneBatch();
            if (operations.size() == 0) {
               this.notifyExecutionFinishedListeners();
               ThreadUtil.sleepFor(2000L);
            }
         } catch (Exception var2) {
            logger.error("Something went wrong, while executing operations batch. Will be retried.", var2);
            ThreadUtil.sleepFor(2000L);
         }
      }

   }

   public List executeOneBatch() throws PersistenceException {
      List futures = new ArrayList();
      List lockedOperations = this.batchOperationWriter.lockBatch();
      Iterator var3 = lockedOperations.iterator();

      while(var3.hasNext()) {
         OperationEntity operation = (OperationEntity)var3.next();
         OperationHandler handler = (OperationHandler)this.getOperationHandlers().get(operation.getType());
         if (handler == null) {
            logger.info("Operation {} on worflowInstanceId {} won't be processed, as no suitable handler was found.", operation.getType(), operation.getProcessInstanceKey());
         } else {
            OperationCommand operationCommand = new OperationCommand(operation, handler);
            futures.add(this.operationsTaskExecutor.submit(operationCommand));
         }
      }

      return futures;
   }

   @Bean
   public Map getOperationHandlers() {
      Map handlerMap = new HashMap();
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         OperationHandler handler = (OperationHandler)var2.next();
         handler.getTypes().forEach((t) -> {
            handlerMap.put(t, handler);
         });
      }

      return handlerMap;
   }

   @Bean({"operationsThreadPoolExecutor"})
   public ThreadPoolTaskExecutor getOperationsTaskExecutor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(this.operateProperties.getOperationExecutor().getThreadsCount());
      executor.setMaxPoolSize(this.operateProperties.getOperationExecutor().getThreadsCount());
      executor.setQueueCapacity(this.operateProperties.getOperationExecutor().getQueueSize());
      executor.setRejectedExecutionHandler(new BlockCallerUntilExecutorHasCapacity());
      executor.setThreadNamePrefix("operation_executor_");
      executor.initialize();
      return executor;
   }

   public void registerListener(ExecutionFinishedListener listener) {
      this.listeners.add(listener);
   }

   private void notifyExecutionFinishedListeners() {
      Iterator var1 = this.listeners.iterator();

      while(var1.hasNext()) {
         ExecutionFinishedListener listener = (ExecutionFinishedListener)var1.next();
         listener.onExecutionFinished();
      }

   }

   private class BlockCallerUntilExecutorHasCapacity implements RejectedExecutionHandler {
      public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
         if (!executor.isShutdown()) {
            try {
               executor.getQueue().put(runnable);
            } catch (InterruptedException var4) {
               Thread.currentThread().interrupt();
            }
         }

      }
   }
}
