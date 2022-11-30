package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.manager.ElasticsearchManager;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProcessInstanceService2 implements IProcessInstanceService2 {

    private final ElasticsearchManager elasticsearchManager;

    public ProcessInstanceService2(ElasticsearchManager elasticsearchManager) {
        this.elasticsearchManager = elasticsearchManager;
    }

    @Override
    public Page<ListViewProcessInstanceDto> pageProcessInstanceByZeebeKey(Long envId,
                                                                          List<Long> zeebeKeyList, int pageNum, int pageSize, Map<String, Object> extra) {
        if(CollUtil.isEmpty(zeebeKeyList)) return new Page<>(0, ListUtil.empty());

        List<ListViewProcessInstanceDto> processInstanceDtoList = listProcessInstanceByZeebeKey(envId, zeebeKeyList);

        PageEnhancedUtil.setFirstPageNo(1);
        List<ListViewProcessInstanceDto> records = PageEnhancedUtil.slice(pageNum, pageSize, processInstanceDtoList);

        Page<ListViewProcessInstanceDto> page = new Page<>(processInstanceDtoList.size(), records);
        Map<String, Object> extraMap = new HashMap<>();
        extraMap.put("successCount", 1);
        extraMap.put("failCount", 1);
        extraMap.put("activeCount", 1);
        extraMap.put("cancleCount", 1);
        page.setExtraMap(extraMap);
        return page;
    }

    @Override
    public List<ListViewProcessInstanceDto> listProcessInstanceByZeebeKey(Long envId, List<Long> zeebeKeyList) {
        if(CollUtil.isEmpty(zeebeKeyList)) return ListUtil.empty();

        List<FieldValue> values = zeebeKeyList.stream().map(key -> new FieldValue.Builder().longValue(key).build())
                .collect(Collectors.toList());

        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build(), new Query.Builder()
                                .terms(new TermsQuery.Builder()
                                        .field(ListViewTemplate.PROCESS_KEY)
                                        .terms(new TermsQueryField.Builder().value(values).build())
                                        .build())
                                .build())
                        .build())
                .build();

        ElasticsearchClient elasticsearchClient = elasticsearchManager.getByEnvId(envId);
        ListViewDao listViewDao = ListViewDao.getInstance(elasticsearchClient);
        List<ProcessInstanceForListViewEntity> processInstanceForListViewEntities = listViewDao.scrollAll(query, ElasticsearchUtil.QueryType.ALL);

        return processInstanceForListViewEntities.stream().map(entity -> {
            ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
            dto.from(entity);
            return dto;
        }).sorted((o1, o2) -> {
            LocalDateTime t1 = Optional.ofNullable(o1.getStartDate()).orElse(LocalDateTime.MIN);
            LocalDateTime t2 = Optional.ofNullable(o2.getStartDate()).orElse(LocalDateTime.MIN);
            return t2.compareTo(t1);
        }).collect(Collectors.toList());
    }

}
