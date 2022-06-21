package io.camunda.operate.schema.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.es.RetryElasticsearchClient;
import io.camunda.operate.exceptions.MigrationException;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.schema.indices.MigrationRepositoryIndex;
import io.camunda.operate.util.ElasticsearchUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchStepsRepository implements StepsRepository {
   private static final Logger logger = LoggerFactory.getLogger(ElasticsearchStepsRepository.class);
   private static final String STEP_FILE_EXTENSION = ".json";
   private static final String DEFAULT_SCHEMA_CHANGE_FOLDER = "/schema/change";
   @Autowired
   private RetryElasticsearchClient retryElasticsearchClient;
   @Qualifier("operateObjectMapper")
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   private MigrationRepositoryIndex migrationRepositoryIndex;

   public void updateSteps() throws IOException, MigrationException {
      List stepsFromFiles = this.readStepsFromClasspath();
      List stepsFromRepository = this.findAll();
      Iterator var3 = stepsFromFiles.iterator();

      while(var3.hasNext()) {
         Step step = (Step)var3.next();
         if (!stepsFromRepository.contains(step)) {
            step.setCreatedDate(OffsetDateTime.now());
            logger.info("Add new step {} to repository.", step);
            this.save(step);
         }
      }

      this.retryElasticsearchClient.refresh(this.getName());
   }

   private List readStepsFromClasspath() throws IOException {
      List steps = new ArrayList();
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

      try {
         Resource[] resources = resolver.getResources("/schema/change/*.json");
         Resource[] var4 = resources;
         int var5 = resources.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Resource resource = var4[var6];
            logger.info("Read step {} ", resource.getFilename());
            steps.add(this.readStepFromFile(resource.getInputStream()));
         }

         steps.sort(Step.SEMANTICVERSION_ORDER_COMPARATOR);
         return steps;
      } catch (FileNotFoundException var8) {
         logger.warn("Directory with migration steps was not found: " + var8.getMessage());
         return steps;
      }
   }

   private Step readStepFromFile(InputStream is) throws IOException {
      return (Step)this.objectMapper.readValue(is, Step.class);
   }

   public String getName() {
      return this.migrationRepositoryIndex.getFullQualifiedName();
   }

   protected String idFromStep(Step step) {
      String var10000 = step.getVersion();
      return var10000 + "-" + step.getOrder();
   }

   public void save(Step step) throws MigrationException, IOException {
      boolean createdOrUpdated = this.retryElasticsearchClient.createOrUpdateDocument(this.getName(), this.idFromStep(step), this.objectMapper.writeValueAsString(step));
      if (createdOrUpdated) {
         logger.info("Step {}  saved.", step);
      } else {
         throw new MigrationException(String.format("Error in save step %s:  document wasn't created/updated.", step));
      }
   }

   protected List findBy(Optional<QueryBuilder> query) {
      SearchSourceBuilder searchSpec = (new SearchSourceBuilder()).sort("version.keyword", SortOrder.ASC);
      Objects.requireNonNull(searchSpec);
      query.ifPresent(searchSpec::query);
      SearchRequest request = (new SearchRequest(new String[]{this.getName()})).source(searchSpec).indicesOptions(IndicesOptions.lenientExpandOpen());
      return this.retryElasticsearchClient.searchWithScroll(request, Step.class, this.objectMapper);
   }

   public List findAll() {
      logger.debug("Find all steps from Elasticsearch at {}:{} ", this.operateProperties.getElasticsearch().getHost(), this.operateProperties.getElasticsearch().getPort());
      return this.findBy(Optional.empty());
   }

   public List findNotAppliedFor(String indexName) {
      logger.debug("Find 'not applied steps' for index {} from Elasticsearch at {}:{} ", new Object[]{indexName, this.operateProperties.getElasticsearch().getHost(), this.operateProperties.getElasticsearch().getPort()});
      return this.findBy(Optional.ofNullable(ElasticsearchUtil.joinWithAnd(QueryBuilders.termQuery("indexName.keyword", indexName), QueryBuilders.termQuery("applied", false))));
   }
}
