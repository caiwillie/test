package io.camunda.operate.webapp.es.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.util.ElasticsearchUtil;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractReader {
   @Autowired
   protected RestHighLevelClient esClient;
   @Autowired
   protected ObjectMapper objectMapper;

   protected List scroll(SearchRequest searchRequest, Class clazz) throws IOException {
      return ElasticsearchUtil.scroll(searchRequest, clazz, this.objectMapper, this.esClient);
   }

   protected List scroll(SearchRequest searchRequest, Class clazz, Consumer<Aggregations> aggsProcessor) throws IOException {
      return ElasticsearchUtil.scroll(searchRequest, clazz, this.objectMapper, this.esClient, (Consumer)null, aggsProcessor);
   }

   protected List scroll(SearchRequest searchRequest, Class clazz, Consumer searchHitsProcessor, Consumer aggsProcessor) throws IOException {
      return ElasticsearchUtil.scroll(searchRequest, clazz, this.objectMapper, this.esClient, searchHitsProcessor, aggsProcessor);
   }
}
