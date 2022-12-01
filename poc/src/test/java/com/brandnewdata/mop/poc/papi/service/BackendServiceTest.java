package com.brandnewdata.mop.poc.papi.service;

import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.mop.poc.papi.dao.ReverseProxyDao;
import com.brandnewdata.mop.poc.papi.dao.ReverseProxyEndpointDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class BackendServiceTest {
    @Mock
    ReverseProxyDao proxyDao;
    @Mock
    ReverseProxyEndpointDao proxyEndpointDao;
    @Mock
    Logger log;
    @InjectMocks
    BackendService backendService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBackend() {
        // api\.([\w-]+)\.g2-dev\.brandnewdata\.com
        ReflectionTestUtils.setField(backendService, "domainRegEx", "api\\.([\\w-]+)\\.g2-dev\\.brandnewdata\\.com");

        when(proxyEndpointDao.selectList(any())).thenReturn(ListUtil.empty());
        backendService.getBackend("api.xxx.g2-dev.brandnewdata.com", "/asdasd");
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme