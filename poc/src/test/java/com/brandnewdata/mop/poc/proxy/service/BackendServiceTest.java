package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.mop.poc.proxy.dao.ReverseProxyDao;
import com.brandnewdata.mop.poc.proxy.dao.ReverseProxyEndpointDao;
import com.brandnewdata.mop.poc.proxy.dto.Backend;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

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
        when(proxyEndpointDao.selectList(any())).thenReturn(ListUtil.empty());
        backendService.getBackend("", "uri");
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme