package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.SequenceFlowEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.SequenceFlowTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.ElasticsearchUtil.QueryType;
import java.io.IOException;
import java.util.List;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SequenceFlowReader extends AbstractReader {
   private static final Logger logger = LoggerFactory.getLogger(SequenceFlowReader.class);
   @Autowired
   private SequenceFlowTemplate sequenceFlowTemplate;

   public List getSequenceFlowsByProcessInstanceKey(Long processInstanceKey) {
      TermQueryBuilder processInstanceKeyQuery = QueryBuilders.termQuery("processInstanceKey", processInstanceKey);
      ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(processInstanceKeyQuery);
      SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest(this.sequenceFlowTemplate, QueryType.ALL).source((new SearchSourceBuilder()).query(query).sort("activityId", SortOrder.ASC));

      try {
         return this.scroll(searchRequest, SequenceFlowEntity.class);
      } catch (IOException var7) {
         String message = String.format("Exception occurred, while obtaining sequence flows: %s for processInstanceKey %s", var7.getMessage(), processInstanceKey);
         logger.error(message, var7);
         throw new OperateRuntimeException(message, var7);
      }
   }
}
