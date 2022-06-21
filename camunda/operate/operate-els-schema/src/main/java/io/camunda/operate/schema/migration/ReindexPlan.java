package io.camunda.operate.schema.migration;

import io.camunda.operate.es.RetryElasticsearchClient;
import io.camunda.operate.exceptions.MigrationException;
import io.camunda.operate.util.CollectionUtil;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexPlan implements Plan {
   private static final Logger logger = LoggerFactory.getLogger(ReindexPlan.class);
   private static final String DEFAULT_SCRIPT = "ctx._index = params.dstIndex+'_' + (ctx._index.substring(ctx._index.indexOf('_') + 1, ctx._index.length()))";
   private List steps = List.of();
   private Script script;
   private String srcIndex;
   private String dstIndex;
   private int reindexBatchSize = 1000;
   private int slices;

   public Script getScript() {
      return this.script;
   }

   public ReindexPlan buildScript(Script script) {
      this.script = script;
      return this;
   }

   public String getSrcIndex() {
      return this.srcIndex;
   }

   public String getDstIndex() {
      return this.dstIndex;
   }

   public ReindexPlan setSrcIndex(String srcIndex) {
      this.srcIndex = srcIndex;
      return this;
   }

   public ReindexPlan setDstIndex(String dstIndex) {
      this.dstIndex = dstIndex;
      return this;
   }

   public ReindexPlan buildScript(String scriptContent, Map params) {
      this.script = new Script(ScriptType.INLINE, "painless", scriptContent, params);
      return this;
   }

   public List getSteps() {
      return this.steps;
   }

   public ReindexPlan setSteps(List steps) {
      this.steps = steps;
      return this;
   }

   public void executeOn(RetryElasticsearchClient retryElasticsearchClient) throws MigrationException {
      ReindexRequest reindexRequest = ((new ReindexRequest()).setSourceIndices(new String[]{this.srcIndex + "_*"}).setDestIndex(this.dstIndex + "_").setSlices(this.slices)).setSourceBatchSize(this.reindexBatchSize);
      Optional<String> pipelineName = this.createPipelineFromSteps(retryElasticsearchClient);
      Objects.requireNonNull(reindexRequest);
      pipelineName.ifPresent(reindexRequest::setDestPipeline);
      if (this.script == null) {
         this.buildScript("ctx._index = params.dstIndex+'_' + (ctx._index.substring(ctx._index.indexOf('_') + 1, ctx._index.length()))", Map.of("dstIndex", this.dstIndex));
      }

      reindexRequest.setScript(this.script);

      try {
         retryElasticsearchClient.reindex(reindexRequest);
      } finally {
         Objects.requireNonNull(retryElasticsearchClient);
         pipelineName.ifPresent(retryElasticsearchClient::removePipeline);
      }

   }

   private Optional createPipelineFromSteps(RetryElasticsearchClient retryElasticsearchClient) throws MigrationException {
      if (this.steps.isEmpty()) {
         return Optional.empty();
      } else {
         String name = this.srcIndex + "-to-" + this.dstIndex + "-pipeline";
         boolean added = retryElasticsearchClient.addPipeline(name, this.getPipelineDefinition());
         if (added) {
            return Optional.of(name);
         } else {
            throw new MigrationException(String.format("Couldn't create '%s' pipeline.", name));
         }
      }
   }

   private String getPipelineDefinition() {
      List stepsAsJSON = CollectionUtil.map(this.steps, Step::getContent);
      return "{ \"processors\": [" + String.join(", ", stepsAsJSON) + "] }";
   }

   public String toString() {
      return "ReindexPlan [steps=" + this.steps + ",  srcIndex=" + this.srcIndex + ", dstIndex=" + this.dstIndex + "]";
   }

   public ReindexPlan setBatchSize(int reindexBatchSize) {
      this.reindexBatchSize = reindexBatchSize;
      return this;
   }

   public ReindexPlan setSlices(int slices) {
      this.slices = slices;
      return this;
   }
}
