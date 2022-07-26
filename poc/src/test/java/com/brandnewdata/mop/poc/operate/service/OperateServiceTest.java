package com.brandnewdata.mop.poc.operate.service;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.SimpleAuthentication;
import io.camunda.operate.exception.OperateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

class OperateServiceTest {
    @Spy
    CamundaOperateClient client;
    @InjectMocks
    OperateService operateService;

    @BeforeEach
    void setUp() throws OperateException {
        SimpleAuthentication sa = new SimpleAuthentication("demo", "demo", "http://10.101.53.4:18081");
        client = new CamundaOperateClient.Builder().operateUrl("http://10.101.53.4:18081").authentication(sa).build();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessDefinitionPage() throws OperateException {

    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme