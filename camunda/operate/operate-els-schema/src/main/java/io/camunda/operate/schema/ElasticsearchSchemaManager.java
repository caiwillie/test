package io.camunda.operate.schema;

import io.camunda.operate.es.RetryElasticsearchClient;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.OperateElasticsearchProperties;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.indices.IndexDescriptor;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.PutComponentTemplateRequest;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.cluster.metadata.ComponentTemplate;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("schemaManager")
@Profile({"!test"})
public class ElasticsearchSchemaManager {
   private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSchemaManager.class);
   private static final String NUMBER_OF_SHARDS = "index.number_of_shards";
   private static final String NUMBER_OF_REPLICAS = "index.number_of_replicas";
   private static final String ALIASES = "aliases";
   @Autowired
   private List indexDescriptors;
   @Autowired
   private List templateDescriptors;
   @Autowired
   protected RetryElasticsearchClient retryElasticsearchClient;
   @Autowired
   protected OperateProperties operateProperties;

   public void createSchema() {
      this.createDefaults();
      this.createTemplates();
      this.createIndices();
   }

   private String settingsTemplateName() {
      OperateElasticsearchProperties elsConfig = this.operateProperties.getElasticsearch();
      return String.format("%s_template", elsConfig.getIndexPrefix());
   }

   private Settings getIndexSettings() {
      OperateElasticsearchProperties elsConfig = this.operateProperties.getElasticsearch();
      return Settings.builder().put("index.number_of_shards", elsConfig.getNumberOfShards()).put("index.number_of_replicas", elsConfig.getNumberOfReplicas()).build();
   }

   private void createDefaults() {
      OperateElasticsearchProperties elsConfig = this.operateProperties.getElasticsearch();
      String settingsTemplate = this.settingsTemplateName();
      logger.info("Create default settings from '{}' with {} shards and {} replicas per index.", new Object[]{settingsTemplate, elsConfig.getNumberOfShards(), elsConfig.getNumberOfReplicas()});
      Settings settings = this.getIndexSettings();
      Template template = new Template(settings, (CompressedXContent)null, (Map)null);
      ComponentTemplate componentTemplate = new ComponentTemplate(template, (Long)null, (Map)null);
      PutComponentTemplateRequest request = (new PutComponentTemplateRequest()).name(settingsTemplate).componentTemplate(componentTemplate);
      this.retryElasticsearchClient.createComponentTemplate(request);
   }

   private void createIndices() {
      this.indexDescriptors.forEach(this::createIndex);
   }

   private void createTemplates() {
      this.templateDescriptors.forEach(this::createTemplate);
   }

   private void createIndex(IndexDescriptor indexDescriptor) {
      String indexFilename = String.format("/schema/create/index/operate-%s.json", indexDescriptor.getIndexName());
      Map indexDescription = this.prepareCreateIndex(indexFilename, indexDescriptor.getAlias());
      this.createIndex((new CreateIndexRequest(indexDescriptor.getFullQualifiedName())).source(indexDescription).settings(this.getIndexSettings()), indexDescriptor.getFullQualifiedName());
   }

   private void createTemplate(TemplateDescriptor templateDescriptor) {
      Template template = this.getTemplateFrom(templateDescriptor);
      ComposableIndexTemplate composableTemplate = (new ComposableIndexTemplate.Builder()).indexPatterns(List.of(templateDescriptor.getIndexPattern())).template(template).componentTemplates(List.of(this.settingsTemplateName())).build();
      this.putIndexTemplate((new PutComposableIndexTemplateRequest()).name(templateDescriptor.getTemplateName()).indexTemplate(composableTemplate));
      String indexName = templateDescriptor.getFullQualifiedName();
      this.createIndex(new CreateIndexRequest(indexName), indexName);
   }

   private Template getTemplateFrom(TemplateDescriptor templateDescriptor) {
      String templateFilename = String.format("/schema/create/template/operate-%s.json", templateDescriptor.getIndexName());
      Map templateConfig = this.readJSONFileToMap(templateFilename);
      PutIndexTemplateRequest ptr = (new PutIndexTemplateRequest(templateDescriptor.getTemplateName())).source(templateConfig);

      try {
         Map aliases = Map.of(templateDescriptor.getAlias(), AliasMetadata.builder(templateDescriptor.getAlias()).build());
         return new Template(ptr.settings(), new CompressedXContent(ptr.mappings()), aliases);
      } catch (IOException var6) {
         throw new OperateRuntimeException(String.format("Error in reading mappings for %s ", templateDescriptor.getTemplateName()), var6);
      }
   }

   private Map prepareCreateIndex(String fileName, String alias) {
      Map indexDescription = this.readJSONFileToMap(fileName);
      indexDescription.put("aliases", Collections.singletonMap(alias, Collections.emptyMap()));
      return indexDescription;
   }

   private Map<String, Object> readJSONFileToMap(String filename) {
      try {
         InputStream inputStream = ElasticsearchSchemaManager.class.getResourceAsStream(filename);

         Map<String, Object> result;
         try {
            if (inputStream == null) {
               throw new OperateRuntimeException("Failed to find " + filename + " in classpath ");
            }

            result = XContentHelper.convertToMap(XContentType.JSON.xContent(), inputStream, true);
         } catch (Throwable var7) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return result;
      } catch (IOException var8) {
         throw new OperateRuntimeException("Failed to load file " + filename + " from classpath ", var8);
      }
   }

   private void createIndex(CreateIndexRequest createIndexRequest, String indexName) {
      boolean created = this.retryElasticsearchClient.createIndex(createIndexRequest);
      if (created) {
         logger.debug("Index [{}] was successfully created", indexName);
      } else {
         logger.debug("Index [{}] was NOT created", indexName);
      }

   }

   private void putIndexTemplate(PutComposableIndexTemplateRequest request) {
      boolean created = this.retryElasticsearchClient.createTemplate(request);
      if (created) {
         logger.debug("Template [{}] was successfully created", request.name());
      } else {
         logger.debug("Template [{}] was NOT created", request.name());
      }

   }
}
