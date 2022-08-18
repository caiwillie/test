package com.brandnewdata.mop.poc.operate.dao;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.GetRequest;
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
                .query(new Query.Builder()
                        .term(t -> t.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(request.getProcessInstanceId()))
                        .build())
                .build();
        return ElasticsearchUtil.scrollAll(client, searchRequest, FlowNodeInstanceEntity.class);
    }

    public void getFlowNodeMetadata() {

    }

    public void getFlowNodeStates() {

    }

    private FlowNodeInstanceEntity getFlowNodeInstance(String flowNodeInstanceId) {

        GetRequest getRequest = new GetRequest.Builder()
                .index(template.getAlias())
                .id(flowNodeInstanceId)
                .build();
        
        return ElasticsearchUtil.getOne(client, getRequest, FlowNodeInstanceEntity.class);
    }

}