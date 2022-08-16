package com.brandnewdata.mop.poc.operate.dao;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceRequest;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlowNodeInstanceDao extends AbstractDao{

    @Autowired
    private FlowNodeInstanceTemplate template;

    public List<FlowNodeInstanceEntity> queryFlowNodeInstances(FlowNodeInstanceRequest request) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(q -> q.term(term -> term.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(request.getProcessInstanceKey())))
                .build();

        return ElasticsearchUtil.scrollAll(client, searchRequest, FlowNodeInstanceEntity.class);
    }

}
