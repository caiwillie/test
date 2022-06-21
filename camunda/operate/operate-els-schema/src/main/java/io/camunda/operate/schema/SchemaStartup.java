package io.camunda.operate.schema;

import io.camunda.operate.exceptions.MigrationException;
import io.camunda.operate.property.MigrationProperties;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.migration.Migrator;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("schemaStartup")
@Profile({"!test"})
public class SchemaStartup {
   private static final Logger LOGGER = LoggerFactory.getLogger(SchemaStartup.class);
   @Autowired
   private ElasticsearchSchemaManager schemaManager;
   @Autowired
   private IndexSchemaValidator schemaValidator;
   @Autowired
   private Migrator migrator;
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   private MigrationProperties migrationProperties;

   @PostConstruct
   public void initializeSchema() throws MigrationException {
      LOGGER.info("SchemaStartup started.");
      LOGGER.info("SchemaStartup: validate schema.");
      this.schemaValidator.validate();
      if (this.operateProperties.getElasticsearch().isCreateSchema() && !this.schemaValidator.schemaExists()) {
         LOGGER.info("SchemaStartup: schema is empty or not complete. Indices will be created.");
         this.schemaManager.createSchema();
      } else {
         LOGGER.info("SchemaStartup: schema won't be created, it either already exist, or schema creation is disabled in configuration.");
      }

      if (this.migrationProperties.isMigrationEnabled()) {
         LOGGER.info("SchemaStartup: migrate schema.");
         this.migrator.migrate();
      }

      LOGGER.info("SchemaStartup finished.");
   }
}
