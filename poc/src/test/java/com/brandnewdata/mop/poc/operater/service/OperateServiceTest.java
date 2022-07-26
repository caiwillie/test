package com.brandnewdata.mop.poc.operater.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.SimpleAuthentication;
import io.camunda.operate.exception.OperateException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.List;

import static org.mockito.Mockito.*;

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
        operateService.processDefinitionPage();
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme