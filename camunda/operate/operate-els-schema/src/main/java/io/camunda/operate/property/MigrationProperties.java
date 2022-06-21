package io.camunda.operate.property;

import io.camunda.operate.exceptions.OperateRuntimeException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties("camunda.operate.migration")
public class MigrationProperties {
   private static final int DEFAULT_REINDEX_BATCH_SIZE = 5000;
   private static final int DEFAULT_THREADS_COUNT = 5;
   private boolean migrationEnabled = true;
   private boolean deleteSrcSchema = true;
   private String sourceVersion;
   private String destinationVersion;
   private int threadsCount = 5;
   private int reindexBatchSize = 5000;
   private int slices = 0;

   public boolean isMigrationEnabled() {
      return this.migrationEnabled;
   }

   public MigrationProperties setMigrationEnabled(boolean migrationEnabled) {
      this.migrationEnabled = migrationEnabled;
      return this;
   }

   public String getSourceVersion() {
      return this.sourceVersion;
   }

   public MigrationProperties setSourceVersion(String sourceVersion) {
      this.sourceVersion = sourceVersion;
      return this;
   }

   public String getDestinationVersion() {
      return this.destinationVersion;
   }

   public MigrationProperties setDestinationVersion(String destinationVersion) {
      this.destinationVersion = destinationVersion;
      return this;
   }

   public MigrationProperties setDeleteSrcSchema(boolean deleteSrcSchema) {
      this.deleteSrcSchema = deleteSrcSchema;
      return this;
   }

   public boolean isDeleteSrcSchema() {
      return this.deleteSrcSchema;
   }

   public int getReindexBatchSize() {
      return this.reindexBatchSize;
   }

   public MigrationProperties setReindexBatchSize(int reindexBatchSize) {
      if (reindexBatchSize >= 1 && reindexBatchSize <= 10000) {
         this.reindexBatchSize = reindexBatchSize;
         return this;
      } else {
         throw new OperateRuntimeException(String.format("Reindex batch size must be between 1 and 10000. Given was %d", reindexBatchSize));
      }
   }

   public int getSlices() {
      return this.slices;
   }

   public MigrationProperties setSlices(int slices) {
      if (slices < 0) {
         throw new OperateRuntimeException(String.format("Slices must be positive. Given was %d", slices));
      } else {
         this.slices = slices;
         return this;
      }
   }

   public int getThreadsCount() {
      return this.threadsCount;
   }

   public void setThreadsCount(int threadsCount) {
      this.threadsCount = threadsCount;
   }
}
