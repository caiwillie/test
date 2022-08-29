package com.brandnewdata.mop.poc.operate.dao;

import cn.hutool.core.lang.Assert;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListViewDao extends AbstractDao{

    @Autowired
    private ListViewTemplate template;

    public ProcessInstanceForListViewEntity getOneByParentFlowNodeInstanceId(String parentFlowNodeInstanceId) {
        Assert.notNull(parentFlowNodeInstanceId);
        SearchRequest request = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(new Query.Builder()
                        .term(new TermQuery.Builder()
                                .field(ListViewTemplate.PARENT_FLOW_NODE_INSTANCE_KEY)
                                .value(parentFlowNodeInstanceId)
                                .build())
                        .build())
                .build();
        return ElasticsearchUtil.searchOne(client, request, ProcessInstanceForListViewEntity.class);
    }

    public ProcessInstanceForListViewEntity searchOne(Query query) {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(query)
                .build();
        return ElasticsearchUtil.searchOne(client, searchRequest, ProcessInstanceForListViewEntity.class);
    }

    public List<ProcessInstanceForListViewEntity> scrollAll(Query query) {
        SearchRequest request = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(query)
                .build();
        return ElasticsearchUtil.scrollAll(client, request, ProcessInstanceForListViewEntity.class);
    }

}
