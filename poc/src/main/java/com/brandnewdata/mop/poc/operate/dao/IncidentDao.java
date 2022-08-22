package com.brandnewdata.mop.poc.operate.dao;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ConstantScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.IncidentEntity;
import com.brandnewdata.mop.poc.operate.schema.template.IncidentTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncidentDao extends AbstractDao {

    @Autowired
    private IncidentTemplate template;

    public IncidentEntity getOneByTreePath(String treePath) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(new Query.Builder()
                        .constantScore(new ConstantScoreQuery.Builder()
                                .filter(new Query.Builder()
                                        .bool(new BoolQuery.Builder()
                                                .must(
                                                        new Query.Builder().term(t -> t.field(IncidentTemplate.TREE_PATH).value(treePath)).build(),
                                                        new Query.Builder().term(t -> t.field(IncidentTemplate.STATE).value("ACTIVE")).build()
                                                )
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        return ElasticsearchUtil.searchOne(client, searchRequest, IncidentEntity.class);
    }

}
