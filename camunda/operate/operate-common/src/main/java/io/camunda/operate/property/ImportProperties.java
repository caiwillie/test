package io.camunda.operate.property;

public class ImportProperties {
   private static final int DEFAULT_IMPORT_THREADS_COUNT = 3;
   private static final int DEFAULT_POST_IMPORT_THREADS_COUNT = 1;
   private static final int DEFAULT_READER_THREADS_COUNT = 3;
   private static final int DEFAULT_IMPORT_QUEUE_SIZE = 3;
   private static final int DEFAULT_READER_BACKOFF = 5000;
   private static final int DEFAULT_SCHEDULER_BACKOFF = 5000;
   private static final int DEFAULT_FLOW_NODE_TREE_CACHE_SIZE = 1000;
   public static final int DEFAULT_VARIABLE_SIZE_THRESHOLD = 8191;
   private int threadsCount = 3;
   private int postImportThreadsCount = 1;
   private int readerThreadsCount = 3;
   private int queueSize = 3;
   private int readerBackoff = 5000;
   private int schedulerBackoff = 5000;
   private int flowNodeTreeCacheSize = 1000;
   private boolean startLoadingDataOnStartup = true;
   private int variableSizeThreshold = 8191;

   public boolean isStartLoadingDataOnStartup() {
      return this.startLoadingDataOnStartup;
   }

   public void setStartLoadingDataOnStartup(boolean startLoadingDataOnStartup) {
      this.startLoadingDataOnStartup = startLoadingDataOnStartup;
   }

   public int getThreadsCount() {
      return this.threadsCount;
   }

   public void setThreadsCount(int threadsCount) {
      this.threadsCount = threadsCount;
   }

   public int getPostImportThreadsCount() {
      return this.postImportThreadsCount;
   }

   public ImportProperties setPostImportThreadsCount(int postImportThreadsCount) {
      this.postImportThreadsCount = postImportThreadsCount;
      return this;
   }

   public int getReaderThreadsCount() {
      return this.readerThreadsCount;
   }

   public ImportProperties setReaderThreadsCount(int readerThreadsCount) {
      this.readerThreadsCount = readerThreadsCount;
      return this;
   }

   public int getQueueSize() {
      return this.queueSize;
   }

   public void setQueueSize(int queueSize) {
      this.queueSize = queueSize;
   }

   public int getReaderBackoff() {
      return this.readerBackoff;
   }

   public void setReaderBackoff(int readerBackoff) {
      this.readerBackoff = readerBackoff;
   }

   public int getSchedulerBackoff() {
      return this.schedulerBackoff;
   }

   public void setSchedulerBackoff(int schedulerBackoff) {
      this.schedulerBackoff = schedulerBackoff;
   }

   public int getFlowNodeTreeCacheSize() {
      return this.flowNodeTreeCacheSize;
   }

   public void setFlowNodeTreeCacheSize(int flowNodeTreeCacheSize) {
      this.flowNodeTreeCacheSize = flowNodeTreeCacheSize;
   }

   public int getVariableSizeThreshold() {
      return this.variableSizeThreshold;
   }

   public ImportProperties setVariableSizeThreshold(int variableSizeThreshold) {
      this.variableSizeThreshold = variableSizeThreshold;
      return this;
   }
}
