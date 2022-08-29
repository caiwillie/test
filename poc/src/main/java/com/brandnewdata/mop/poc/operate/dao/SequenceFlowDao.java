package com.brandnewdata.mop.poc.operate.dao;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.SequenceFlowEntity;
import com.brandnewdata.mop.poc.operate.schema.template.SequenceFlowTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SequenceFlowDao extends AbstractDao {

    @Autowired
    private SequenceFlowTemplate template;

    public List<SequenceFlowEntity> scrollAll(Query query) {
        SearchRequest request = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(query)
                .build();
        return ElasticsearchUtil.scrollAll(client, request, SequenceFlowEntity.class);
    }

}
