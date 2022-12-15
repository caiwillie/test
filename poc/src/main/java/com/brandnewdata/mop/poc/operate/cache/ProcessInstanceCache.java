package com.brandnewdata.mop.poc.operate.cache;

import cn.hutool.core.date.DateUtil;
import cn.hutool.cron.Scheduler;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.ProcessInstanceStateDto;
import com.brandnewdata.mop.poc.operate.manager.ElasticsearchManager;
import com.brandnewdata.mop.poc.util.HttpHostUtil;
import com.caiwillie.util.cache.ScheduleScanEsCache;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
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
        init();
    }

    private void init() {
        HttpHost httpHost = HttpHostUtil.createHttpHost("es-connector1.basic.dev.brandnewdata.com:8080");

        // Create the low-level client
        RestClient restClient = RestClient.builder(new HttpHost[]{httpHost}).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(JacksonUtil.getObjectMapper()));

        // And create the API client
        // ElasticsearchClient client = new ElasticsearchClient(transport);

        Scheduler scheduler = new Scheduler();
        scheduler.setMatchSecond(true);
        scheduler.schedule("0/10 * * * * ?", (Runnable) () -> {
            List<EnvDto> envDtoList = envService.fetchEnvList();
            for (EnvDto envDto : envDtoList) {
                Long id = envDto.getId();
                // 存在就放过
                if(cacheMap.containsKey(id)) continue;
                ElasticsearchClient client = elasticsearchManager.getByEnvId(id);
                if(client == null) continue;

                BoolQuery filter = new BoolQuery.Builder()
                        .must(new Query.Builder().term(t -> t.field("joinRelation").value("processInstance")).build())
                        .build();

                ScheduleScanEsCache<String, ListViewProcessInstanceDto> cache = new ScheduleScanEsCache<>(
                        "operate-list-view-1.3.0_alias", "id",
                        "startDate", client, "0/4 * * * * ?", filter, getConsume());

/*
                ScheduleScanEsCache<String, ListViewProcessInstanceDto> cache = new ScheduleScanEsCache<>(
                        "operate-list-view-1.3.0_alias", "id",
                        "startDate", client, "0/4 * * * * ?", filter, getConsume());
*/

                cacheMap.put(id, cache);
            }
        });
        scheduler.start();

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
        dto.setProcessId(objectNode.path("processDefinitionKey").longValue());
        dto.setStartDate(DateUtil.parse(objectNode.path("startDate").textValue()).toLocalDateTime());
        dto.setBpmnProcessId(objectNode.path("bpmnProcessId").textValue());
        dto.setState(ProcessInstanceStateDto.valueOf(objectNode.path("state").textValue()));
        return dto;
    }
}
