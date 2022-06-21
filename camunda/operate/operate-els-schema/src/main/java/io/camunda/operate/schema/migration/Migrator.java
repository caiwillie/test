package io.camunda.operate.schema.migration;

import io.camunda.operate.es.RetryElasticsearchClient;
import io.camunda.operate.exceptions.MigrationException;
import io.camunda.operate.property.MigrationProperties;
import io.camunda.operate.property.OperateElasticsearchProperties;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.IndexSchemaValidator;
import io.camunda.operate.schema.indices.IndexDescriptor;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.CollectionUtil;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.elasticsearch.common.settings.Settings;
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
public class Migrator {
   private static final Logger logger = LoggerFactory.getLogger(Migrator.class);
   @Autowired
   private List indexDescriptors;
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   private RetryElasticsearchClient retryElasticsearchClient;
   @Autowired
   private StepsRepository stepsRepository;
   @Autowired
   private MigrationProperties migrationProperties;
   @Autowired
   @Qualifier("migrationThreadPoolExecutor")
   private ThreadPoolTaskExecutor migrationExecutor;
   @Autowired
   private IndexSchemaValidator indexSchemaValidator;

   @Bean({"migrationThreadPoolExecutor"})
   public ThreadPoolTaskExecutor getTaskExecutor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(this.migrationProperties.getThreadsCount());
      executor.setMaxPoolSize(this.migrationProperties.getThreadsCount());
      executor.setThreadNamePrefix("migration_");
      executor.initialize();
      return executor;
   }

   public void migrate() throws MigrationException {
      try {
         this.stepsRepository.updateSteps();
      } catch (IOException var7) {
         throw new MigrationException(String.format("Migration failed due to %s", var7.getMessage()));
      }

      boolean failed = false;
      List results = (List)this.indexDescriptors.stream().map(this::migrateIndexInThread).collect(Collectors.toList());
      Iterator var3 = results.iterator();

      while(var3.hasNext()) {
         Future result = (Future)var3.next();

         try {
            if (!(Boolean)result.get()) {
               failed = true;
            }
         } catch (Exception var6) {
            logger.error("Migration failed: ", var6);
            failed = true;
         }
      }

      this.migrationExecutor.shutdown();
      if (failed) {
         throw new MigrationException("Migration failed. See logging messages above.");
      }
   }

   private Future migrateIndexInThread(IndexDescriptor indexDescriptor) {
      return this.migrationExecutor.submit(() -> {
         try {
            this.migrateIndexIfNecessary(indexDescriptor);
         } catch (Exception var3) {
            logger.error("Migration for {} failed:", indexDescriptor.getIndexName(), var3);
            return false;
         }

         return true;
      });
   }

   private void migrateIndexIfNecessary(IndexDescriptor indexDescriptor) throws MigrationException, IOException {
      logger.info("Check if index {} needs to migrate.", indexDescriptor.getIndexName());
      Set olderVersions = this.indexSchemaValidator.olderVersionsForIndex(indexDescriptor);
      if (olderVersions.size() > 1) {
         throw new MigrationException(String.format("For index %s are existing more than one older versions: %s ", indexDescriptor.getIndexName(), olderVersions));
      } else {
         if (olderVersions.isEmpty()) {
            logger.info("No migration needed for {}, no previous indices found.", indexDescriptor.getIndexName());
         } else {
            String olderVersion = (String)olderVersions.iterator().next();
            String currentVersion = indexDescriptor.getVersion();
            List stepsForIndex = this.stepsRepository.findNotAppliedFor(indexDescriptor.getIndexName());
            Plan plan = this.createPlanFor(indexDescriptor.getIndexName(), olderVersion, currentVersion, stepsForIndex);
            this.migrateIndex(indexDescriptor, plan);
            if (this.migrationProperties.isDeleteSrcSchema()) {
               String olderBaseIndexName = String.format("%s-%s-%s_", this.operateProperties.getElasticsearch().getIndexPrefix(), indexDescriptor.getIndexName(), olderVersion);
               String deleteIndexPattern = String.format("%s*", olderBaseIndexName);
               logger.info("Deleted previous indices for pattern {}", deleteIndexPattern);
               this.retryElasticsearchClient.deleteIndicesFor(deleteIndexPattern);
               if (indexDescriptor instanceof TemplateDescriptor) {
                  String deleteTemplatePattern = String.format("%stemplate", olderBaseIndexName);
                  logger.info("Deleted previous templates for {}", deleteTemplatePattern);
                  this.retryElasticsearchClient.deleteTemplatesFor(deleteTemplatePattern);
               }
            }
         }

      }
   }

   public void migrateIndex(IndexDescriptor indexDescriptor, Plan plan) throws IOException, MigrationException {
      OperateElasticsearchProperties elsConfig = this.operateProperties.getElasticsearch();
      logger.debug("Save current settings for {}", indexDescriptor.getFullQualifiedName());
      Map indexSettings = this.getIndexSettingsOrDefaultsFor(indexDescriptor, elsConfig);
      logger.debug("Set reindex settings for {}", indexDescriptor.getDerivedIndexNamePattern());
      this.retryElasticsearchClient.setIndexSettingsFor(Settings.builder().put("index.number_of_replicas", "0").put("index.refresh_interval", "-1").build(), indexDescriptor.getDerivedIndexNamePattern());
      logger.info("Execute plan: {} ", plan);
      plan.executeOn(this.retryElasticsearchClient);
      logger.debug("Save applied steps in migration repository");
      Iterator var5 = plan.getSteps().iterator();

      while(var5.hasNext()) {
         Step step = (Step)var5.next();
         step.setApplied(true).setAppliedDate(OffsetDateTime.now());
         this.stepsRepository.save(step);
      }

      logger.debug("Restore settings for {}", indexDescriptor.getDerivedIndexNamePattern());
      this.retryElasticsearchClient.setIndexSettingsFor(Settings.builder().put("index.number_of_replicas", (String)indexSettings.get("index.number_of_replicas")).put("index.refresh_interval", (String)indexSettings.get("index.refresh_interval")).build(), indexDescriptor.getDerivedIndexNamePattern());
      logger.info("Refresh index {}", indexDescriptor.getDerivedIndexNamePattern());
      this.retryElasticsearchClient.refresh(indexDescriptor.getDerivedIndexNamePattern());
   }

   private Map getIndexSettingsOrDefaultsFor(IndexDescriptor indexDescriptor, OperateElasticsearchProperties elsConfig) {
      Map settings = new HashMap();
      settings.put("index.refresh_interval", this.retryElasticsearchClient.getOrDefaultRefreshInterval(indexDescriptor.getFullQualifiedName(), elsConfig.getRefreshInterval()));
      settings.put("index.number_of_replicas", this.retryElasticsearchClient.getOrDefaultNumbersOfReplica(indexDescriptor.getFullQualifiedName(), "" + elsConfig.getNumberOfReplicas()));
      return settings;
   }

   protected Plan createPlanFor(String indexName, String srcVersion, String dstVersion, List steps) {
      SemanticVersion sourceVersion = SemanticVersion.fromVersion(srcVersion);
      SemanticVersion destinationVersion = SemanticVersion.fromVersion(dstVersion);
      List sortByVersion = new ArrayList(steps);
      sortByVersion.sort(Step.SEMANTICVERSION_ORDER_COMPARATOR);
      List onlyAffectedVersions = CollectionUtil.filter(sortByVersion, (s) -> {
         return SemanticVersion.fromVersion(s.getVersion()).isBetween(sourceVersion, destinationVersion);
      });
      String indexPrefix = this.operateProperties.getElasticsearch().getIndexPrefix();
      String srcIndex = String.format("%s-%s-%s", indexPrefix, indexName, srcVersion);
      String dstIndex = String.format("%s-%s-%s", indexPrefix, indexName, dstVersion);
      return Plan.forReindex().setBatchSize(this.migrationProperties.getReindexBatchSize()).setSlices(this.migrationProperties.getSlices()).setSrcIndex(srcIndex).setDstIndex(dstIndex).setSteps(onlyAffectedVersions);
   }
}
