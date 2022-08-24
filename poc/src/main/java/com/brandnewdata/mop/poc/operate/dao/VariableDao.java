package com.brandnewdata.mop.poc.operate.dao;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.VariableEntity;
import com.brandnewdata.mop.poc.operate.schema.template.VariableTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VariableDao extends AbstractDao {

    @Autowired
    private VariableTemplate template;


    public List<VariableEntity> list(Query query) {
        SearchRequest request = new SearchRequest.Builder()
                .index(template.getFullQualifiedName())
                .query(query)
                .build();
        return ElasticsearchUtil.scrollAll(client, request, VariableEntity.class);
    }

}
