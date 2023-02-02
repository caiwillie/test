package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.ListUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.cache.ProcessInstanceCache;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDto;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.SequenceFlowDto;
import com.brandnewdata.mop.poc.operate.dto.filter.ProcessInstanceFilter;
import com.brandnewdata.mop.poc.operate.dto.statistic.ProcessInstanceAgg;
import com.brandnewdata.mop.poc.operate.manager.DaoManager;
import com.brandnewdata.mop.poc.operate.manager.ElasticsearchManager;
import com.brandnewdata.mop.poc.util.HttpHostUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class ProcessInstanceServiceTest {
    @Mock
    ElasticsearchManager elasticsearchManager;

    @Mock
    ProcessInstanceCache processInstanceCache;

    ProcessInstanceService processInstanceService;

    private static ElasticsearchClient client;
    @BeforeAll
    static void init() {
        HttpHost httpHost = HttpHostUtil.createHttpHost("es-connector1.basic.dev.brandnewdata.com:8080");

        // Create the low-level client
        RestClient restClient = RestClient.builder(new HttpHost[]{httpHost}).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(JacksonUtil.getObjectMapper()));

        // And create the API client
        client = new ElasticsearchClient(transport);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        DaoManager daoManager = new DaoManager(elasticsearchManager);
        processInstanceService = new ProcessInstanceService(daoManager, processInstanceCache);
    }

    @Test
    void testAggProcessInstance() {
        when(elasticsearchManager.getByEnvId(anyLong())).thenReturn(client);
        List<Long> zeebeKeyList = ListUtil.of(2251799813819879L, 2251799813820374L, 2251799813820548L, 2251799813989272L);
        List<ProcessInstanceAgg> result = processInstanceService.aggProcessInstance(1L, zeebeKeyList, new ProcessInstanceFilter());
        return;
    }


}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme