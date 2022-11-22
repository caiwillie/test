package com.brandnewdata.mop.poc.operate.cache;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProcessInstanceNoExpCache {

    @Autowired
    private ListViewDao listViewDao;

    private final Cache<String, ListViewProcessInstanceDto> CACHE = CacheBuilder.newBuilder().build();

    @Scheduled(fixedDelay = 60000)
    public void load() {
        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build())
                        .build())
                .build();

        List<ProcessInstanceForListViewEntity> entities = listViewDao.scrollAll(query, ElasticsearchUtil.QueryType.ALL);

        entities.stream().map(entity -> {
            ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
            dto.from(entity);
            return dto;
        }).forEach(dto -> {
            CACHE.put(dto.getId(), dto);
        });
    }

    public Map<String, ListViewProcessInstanceDto> asMap() {
        return CACHE.asMap();
    }
}
