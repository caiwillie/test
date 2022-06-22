/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.SequenceFlowEntity
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.templates.SequenceFlowTemplate
 *  io.camunda.operate.schema.templates.TemplateDescriptor
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.util.ElasticsearchUtil$QueryType
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.index.query.ConstantScoreQueryBuilder
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.index.query.TermQueryBuilder
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.search.sort.SortOrder
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.es.reader;

import io.camunda.operate.entities.SequenceFlowEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.SequenceFlowTemplate;
import io.camunda.operate.schema.templates.TemplateDescriptor;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import java.io.IOException;
import java.util.List;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SequenceFlowReader
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(SequenceFlowReader.class);
    @Autowired
    private SequenceFlowTemplate sequenceFlowTemplate;

    public List<SequenceFlowEntity> getSequenceFlowsByProcessInstanceKey(Long processInstanceKey) {
        TermQueryBuilder processInstanceKeyQuery = QueryBuilders.termQuery((String)"processInstanceKey", (Object)processInstanceKey);
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery((QueryBuilder)processInstanceKeyQuery);
        SearchRequest searchRequest = ElasticsearchUtil.createSearchRequest((TemplateDescriptor)this.sequenceFlowTemplate, (ElasticsearchUtil.QueryType)ElasticsearchUtil.QueryType.ALL).source(new SearchSourceBuilder().query((QueryBuilder)query).sort("activityId", SortOrder.ASC));
        try {
            return this.scroll(searchRequest, SequenceFlowEntity.class);
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining sequence flows: %s for processInstanceKey %s", e.getMessage(), processInstanceKey);
            logger.error(message, e);
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }
}
