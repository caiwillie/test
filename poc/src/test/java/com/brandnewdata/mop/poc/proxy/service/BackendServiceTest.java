package com.brandnewdata.mop.poc.proxy.service;

import com.brandnewdata.mop.poc.proxy.dao.ProxyDao;
import com.brandnewdata.mop.poc.proxy.dao.ProxyEndpointDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

class BackendServiceTest {
    @Mock
    ProxyDao proxyDao;
    @Mock
    ProxyEndpointDao proxyEndpointDao;
    @Mock
    Logger log;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBackend() {
        // api\.([\w-]+)\.g2-dev\.brandnewdata\.com
        // ReflectionTestUtils.setField(backendService, "domainRegEx", "api\\.([\\w-]+)\\.g2-dev\\.brandnewdata\\.com");
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme