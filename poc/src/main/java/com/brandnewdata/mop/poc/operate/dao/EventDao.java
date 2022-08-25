package com.brandnewdata.mop.poc.operate.dao;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.EventEntity;
import com.brandnewdata.mop.poc.operate.schema.template.EventTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventDao extends AbstractDao {

    @Autowired
    private EventTemplate template;


    @SneakyThrows
    public EventEntity getOne(String flowNodeInstanceId) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(new Query.Builder()
                        .term(t -> t.field(EventTemplate.FLOW_NODE_INSTANCE_KEY).value(flowNodeInstanceId))
                        .build()
                )
                .build();

        return ElasticsearchUtil.searchOne(client, searchRequest, EventEntity.class);
    }

}
