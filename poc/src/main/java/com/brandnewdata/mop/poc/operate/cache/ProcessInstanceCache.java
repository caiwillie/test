package com.brandnewdata.mop.poc.operate.cache;

import cn.hutool.core.date.DateUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto;
import com.brandnewdata.mop.poc.operate.manager.ElasticsearchManager;
import com.caiwillie.util.cache.ScheduleScanEsCache;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class ProcessInstanceCache {

    private final Map<Long, ScheduleScanEsCache<String, ListViewProcessInstanceDto>> cacheMap = new HashMap<>();
    private final ElasticsearchManager elasticsearchManager;
    private final IEnvService envService;

    public ProcessInstanceCache(ElasticsearchManager elasticsearchManager, IEnvService envService) {
        this.elasticsearchManager = elasticsearchManager;
        this.envService = envService;
        // init();
    }

    private void init() {
        List<EnvDto> envDtoList = envService.fetchEnvList();
        for (EnvDto envDto : envDtoList) {
            Long id = envDto.getId();
            ElasticsearchClient client = elasticsearchManager.getByEnvId(id);

            BoolQuery filter = new BoolQuery.Builder()
                    .must(new Query.Builder().term(t -> t.field("joinRelation").value("processInstance")).build())
                    .build();

            ScheduleScanEsCache<String, ListViewProcessInstanceDto> cache = new ScheduleScanEsCache<>("operate-list-view-1.3.0_alias", "id",
                    "startDate", client, "0/4 * * * * ?", filter, getConsume());

            cacheMap.put(id, cache);
        }
    }

    public Map<String, ListViewProcessInstanceDto> asMap(Long envId) {
        return cacheMap.get(envId).asMap();
    }

    private BiConsumer<List<ObjectNode>, Cache<String, ListViewProcessInstanceDto>> getConsume() {
        return (objectNodes, cache) -> {
            for (ObjectNode objectNode : objectNodes) {
                ListViewProcessInstanceDto dto = convert(objectNode);
                cache.put(dto.getId(), dto);
            }
        };
    }

    private ListViewProcessInstanceDto convert(ObjectNode objectNode) {
        ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
        dto.setId(objectNode.path("id").textValue());
        dto.setStartDate(DateUtil.parse(objectNode.path("startDate").textValue()).toLocalDateTime());
        dto.setBpmnProcessId(objectNode.path("bpmnProcessId").textValue());
        dto.setState(ProcessInstanceStateDto.valueOf(objectNode.path("state").textValue()));
        return dto;
    }
}
