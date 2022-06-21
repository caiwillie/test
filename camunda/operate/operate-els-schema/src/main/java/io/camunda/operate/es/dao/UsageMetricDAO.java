package io.camunda.operate.es.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.schema.indices.MetricIndex;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsageMetricDAO extends GenericDAO {
   @Autowired
   public UsageMetricDAO(ObjectMapper objectMapper, MetricIndex index, RestHighLevelClient esClient) {
      super(objectMapper, index, esClient);
   }
}
