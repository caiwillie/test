package com.brandnewdata.mop.poc.operate.cache;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.cron.Scheduler;
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
import io.camunda.operate.dto.ProcessInstanceState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class ProcessInstanceCache {

    private final Map<Long, ScheduleScanEsCache<String, ListViewProcessInstanceDto>> cacheMap = new HashMap<>();
    private final ElasticsearchManager elasticsearchManager;
    private final IEnvService envService;

    private final int maxRowSize;

    public ProcessInstanceCache(ElasticsearchManager elasticsearchManager,
                                IEnvService envService,
                                @Value("${brandnewdata.elasticsearch-schedule.maxRowSize}") int maxRowSize) {
        this.elasticsearchManager = elasticsearchManager;
        this.envService = envService;
        this.maxRowSize = maxRowSize;
        // init();
    }

    private void init() {
        Scheduler scheduler = new Scheduler();
        scheduler.setMatchSecond(true);
        scheduler.schedule("0/10 * * * * ?", (Runnable) () -> {
            List<EnvDto> envDtoList = envService.fetchEnvList();
            for (EnvDto envDto : envDtoList) {
                // ?????????????????????????????????
                if(NumberUtil.equals(envDto.getType(), 1)) continue;
                Long id = envDto.getId();
                // ???????????????
                if(cacheMap.containsKey(id)) continue;
                ElasticsearchClient client = elasticsearchManager.getByEnvId(id);
                if(client == null) continue;

                BoolQuery filter = new BoolQuery.Builder()
                        .must(new Query.Builder().term(t -> t.field("joinRelation").value("processInstance")).build())
                        .build();

                ScheduleScanEsCache<String, ListViewProcessInstanceDto> cache = new ScheduleScanEsCache<>(
                        "operate-list-view-1.3.0_alias", "id",
                        "startDate", client, "0/4 * * * * ?", filter, maxRowSize, getConsume());
                log.info("process instance cache add env {}", envDto.getId());
                cacheMap.put(id, cache);
            }
        });
        scheduler.setThreadExecutor(Executors.newSingleThreadExecutor());
        scheduler.start();
    }

    public Map<String, ListViewProcessInstanceDto> asMap(Long envId) {
        log.debug("process instance cache as map: env {}", envId);
        return Opt.ofNullable(cacheMap.get(envId)).map(ScheduleScanEsCache::asMap).orElse(MapUtil.empty());
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
        dto.setProcessId(objectNode.path("processDefinitionKey").longValue());
        dto.setStartDate(DateUtil.parse(objectNode.path("startDate").textValue()).toLocalDateTime());
        dto.setBpmnProcessId(objectNode.path("bpmnProcessId").textValue());
        ProcessInstanceState state = ProcessInstanceState.valueOf(objectNode.path("state").textValue());
        boolean incident = objectNode.path("incident").booleanValue();
        if(state == ProcessInstanceState.ACTIVE && incident) {
            dto.setState(ProcessInstanceStateDto.INCIDENT);
        } else {
            dto.setState(ProcessInstanceStateDto.getState(state));
        }

        return dto;
    }
}
