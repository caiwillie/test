package com.brandnewdata.mop.poc.proxy.service.atomic;

import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.mop.poc.proxy.cache.ProxyEndpointCallCache;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointCallDao;
import com.brandnewdata.mop.poc.proxy.dto.agg.ProxyEndpointCallAgg;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointCallFilter;
import datasource.MybatisPlusMapperUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class ProxyEndpointCallAServiceTest {
    static ProxyEndpointCallDao proxyEndpointCallDao;

    @Mock
    ProxyEndpointCallCache proxyEndpointCallCache;

    ProxyEndpointCallAService proxyEndpointCallAService;

    @BeforeAll
    static void init() {
        proxyEndpointCallDao = MybatisPlusMapperUtil.get(ProxyEndpointCallDao.class);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        proxyEndpointCallAService = new ProxyEndpointCallAService(proxyEndpointCallDao, proxyEndpointCallCache);
    }

    @Test
    void testAggProxyEndpointCallByEndpointId() {
        List<Long> list = ListUtil.of(1601082433734529024L, 1601085610236137472L, 1602145062579179520L,
                1602590965063446528L, 1602601388797202432L, 1602623657145667584L, 1602623756538089472L);
        List<ProxyEndpointCallAgg> result = proxyEndpointCallAService
                .aggProxyEndpointCallByEndpointId(list, new ProxyEndpointCallFilter());
        return;
    }
}
