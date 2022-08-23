package com.brandnewdata.mop.poc.operate.dao;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.ProcessEntity;
import com.brandnewdata.mop.poc.operate.schema.index.ProcessIndex;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessDao extends AbstractDao {

    @Autowired
    private ProcessIndex processIndex;

    public ProcessEntity getOne(Query query) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(processIndex.getFullQualifiedName())
                .query(query)
                .build();

        return ElasticsearchUtil.searchOne(client, searchRequest, ProcessEntity.class);
    }

}
