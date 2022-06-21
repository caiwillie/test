package io.camunda.operate.schema.indices;

import org.springframework.stereotype.Component;

@Component
public class MigrationRepositoryIndex extends AbstractIndexDescriptor {
   public static final String INDEX_NAME = "migration-steps-repository";

   public String getIndexName() {
      return "migration-steps-repository";
   }

   public String getVersion() {
      return "1.1.0";
   }
}
