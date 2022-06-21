package io.camunda.operate.schema;

import io.camunda.operate.es.RetryElasticsearchClient;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.indices.IndexDescriptor;
import io.camunda.operate.schema.migration.SemanticVersion;
import io.camunda.operate.util.CollectionUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexSchemaValidator {
   private static final Logger logger = LoggerFactory.getLogger(IndexSchemaValidator.class);
   private static final Pattern VERSION_PATTERN = Pattern.compile(".*-(\\d+\\.\\d+\\.\\d+.*)_.*");
   @Autowired
   Set<IndexDescriptor> indexDescriptors;
   @Autowired
   OperateProperties operateProperties;
   @Autowired
   RetryElasticsearchClient retryElasticsearchClient;

   private Set<String> getAllIndexNamesForIndex(String index) {
      String indexPattern = String.format("%s-%s*", this.getIndexPrefix(), index);
      logger.debug("Getting all indices for {}", indexPattern);
      Set<String> indexNames = this.retryElasticsearchClient.getIndexNames(indexPattern);
      String patternWithVersion = String.format("%s-%s-\\d.*", this.getIndexPrefix(), index);
      return indexNames.stream().filter((n) -> {
         return n.matches(patternWithVersion);
      }).collect(Collectors.toSet());
   }

   private String getIndexPrefix() {
      return this.operateProperties.getElasticsearch().getIndexPrefix();
   }

   public Set<String> newerVersionsForIndex(IndexDescriptor indexDescriptor) {
      SemanticVersion currentVersion = SemanticVersion.fromVersion(indexDescriptor.getVersion());
      Set<String> versions = this.versionsForIndex(indexDescriptor);
      return versions.stream().filter((version) -> {
         return SemanticVersion.fromVersion(version).isNewerThan(currentVersion);
      }).collect(Collectors.toSet());
   }

   public Set<String> olderVersionsForIndex(IndexDescriptor indexDescriptor) {
      SemanticVersion currentVersion = SemanticVersion.fromVersion(indexDescriptor.getVersion());
      Set<String> versions = this.versionsForIndex(indexDescriptor);
      return versions.stream().filter((version) -> {
         return currentVersion.isNewerThan(SemanticVersion.fromVersion(version));
      }).collect(Collectors.toSet());
   }

   private Set<String> versionsForIndex(IndexDescriptor indexDescriptor) {
      Set<String> allIndexNames = this.getAllIndexNamesForIndex(indexDescriptor.getIndexName());
      return allIndexNames.stream().map(this::getVersionFromIndexName).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
   }

   private Optional<String> getVersionFromIndexName(String indexName) {
      Matcher matcher = VERSION_PATTERN.matcher(indexName);
      return matcher.matches() && matcher.groupCount() > 0 ? Optional.of(matcher.group(1)) : Optional.empty();
   }

   public void validate() {
      if (this.hasAnyOperateIndices()) {
         Set<String> errors = new HashSet<>();
         this.indexDescriptors.forEach((indexDescriptor) -> {
            Set<String> oldVersions = this.olderVersionsForIndex(indexDescriptor);
            Set<String> newerVersions = this.newerVersionsForIndex(indexDescriptor);
            if (oldVersions.size() > 1) {
               errors.add(String.format("More than one older version for %s (%s) found: %s", indexDescriptor.getIndexName(), indexDescriptor.getVersion(), oldVersions));
            }

            if (!newerVersions.isEmpty()) {
               errors.add(String.format("Newer version(s) for %s (%s) already exists: %s", indexDescriptor.getIndexName(), indexDescriptor.getVersion(), newerVersions));
            }

         });
         if (!errors.isEmpty()) {
            throw new OperateRuntimeException("Error(s) in index schema: " + String.join(";", errors));
         }
      }
   }

   public boolean hasAnyOperateIndices() {
      RetryElasticsearchClient var10000 = this.retryElasticsearchClient;
      String var10001 = this.operateProperties.getElasticsearch().getIndexPrefix();
      Set indices = var10000.getIndexNames(var10001 + "*");
      return !indices.isEmpty();
   }

   public boolean schemaExists() {
      try {
         Set indices = this.retryElasticsearchClient.getIndexNames(this.operateProperties.getElasticsearch().getIndexPrefix() + "*");
         List allIndexNames = CollectionUtil.map(this.indexDescriptors, IndexDescriptor::getFullQualifiedName);
         return indices.containsAll(allIndexNames);
      } catch (Exception var3) {
         logger.error("Check for existing schema failed", var3);
         return false;
      }
   }
}
