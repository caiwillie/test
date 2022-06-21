package io.camunda.operate.property;

public class ArchiverProperties {
   private static final int DEFAULT_ARCHIVER_THREADS_COUNT = 1;
   private boolean rolloverEnabled = true;
   private int threadsCount = 1;
   private String rolloverDateFormat = "yyyy-MM-dd";
   private String elsRolloverDateFormat = "date";
   private String rolloverInterval = "1d";
   private int rolloverBatchSize = 100;
   private String waitPeriodBeforeArchiving = "1h";
   private int delayBetweenRuns = 2000;

   public boolean isRolloverEnabled() {
      return this.rolloverEnabled;
   }

   public void setRolloverEnabled(boolean rolloverEnabled) {
      this.rolloverEnabled = rolloverEnabled;
   }

   public String getRolloverDateFormat() {
      return this.rolloverDateFormat;
   }

   public void setRolloverDateFormat(String rolloverDateFormat) {
      this.rolloverDateFormat = rolloverDateFormat;
   }

   public String getElsRolloverDateFormat() {
      return this.elsRolloverDateFormat;
   }

   public void setElsRolloverDateFormat(String elsRolloverDateFormat) {
      this.elsRolloverDateFormat = elsRolloverDateFormat;
   }

   public String getRolloverInterval() {
      return this.rolloverInterval;
   }

   public void setRolloverInterval(String rolloverInterval) {
      this.rolloverInterval = rolloverInterval;
   }

   public int getRolloverBatchSize() {
      return this.rolloverBatchSize;
   }

   public void setRolloverBatchSize(int rolloverBatchSize) {
      this.rolloverBatchSize = rolloverBatchSize;
   }

   public int getThreadsCount() {
      return this.threadsCount;
   }

   public void setThreadsCount(int threadsCount) {
      this.threadsCount = threadsCount;
   }

   public String getWaitPeriodBeforeArchiving() {
      return this.waitPeriodBeforeArchiving;
   }

   public void setWaitPeriodBeforeArchiving(String waitPeriodBeforeArchiving) {
      this.waitPeriodBeforeArchiving = waitPeriodBeforeArchiving;
   }

   public String getArchivingTimepoint() {
      return "now-" + this.waitPeriodBeforeArchiving;
   }

   public int getDelayBetweenRuns() {
      return this.delayBetweenRuns;
   }

   public void setDelayBetweenRuns(int delayBetweenRuns) {
      this.delayBetweenRuns = delayBetweenRuns;
   }
}
