package io.camunda.operate.property;

import java.util.UUID;

public class OperationExecutorProperties {
   public static final int BATCH_SIZE_DEFAULT = 500;
   public static final String WORKER_ID_DEFAULT = UUID.randomUUID().toString();
   public static final long LOCK_TIMEOUT_DEFAULT = 60000L;
   private static final int DEFAULT_IMPORT_THREADS_COUNT = 3;
   private static final int DEFAULT_IMPORT_QUEUE_SIZE = 10;
   private int batchSize = 500;
   private String workerId;
   private long lockTimeout;
   private boolean executorEnabled;
   private int threadsCount;
   private int queueSize;

   public OperationExecutorProperties() {
      this.workerId = WORKER_ID_DEFAULT;
      this.lockTimeout = 60000L;
      this.executorEnabled = true;
      this.threadsCount = 3;
      this.queueSize = 10;
   }

   public int getBatchSize() {
      return this.batchSize;
   }

   public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
   }

   public String getWorkerId() {
      return this.workerId;
   }

   public void setWorkerId(String workerId) {
      this.workerId = workerId;
   }

   public long getLockTimeout() {
      return this.lockTimeout;
   }

   public void setLockTimeout(long lockTimeout) {
      this.lockTimeout = lockTimeout;
   }

   public boolean isExecutorEnabled() {
      return this.executorEnabled;
   }

   public void setExecutorEnabled(boolean executorEnabled) {
      this.executorEnabled = executorEnabled;
   }

   public int getThreadsCount() {
      return this.threadsCount;
   }

   public void setThreadsCount(int threadsCount) {
      this.threadsCount = threadsCount;
   }

   public int getQueueSize() {
      return this.queueSize;
   }

   public void setQueueSize(int queueSize) {
      this.queueSize = queueSize;
   }
}
