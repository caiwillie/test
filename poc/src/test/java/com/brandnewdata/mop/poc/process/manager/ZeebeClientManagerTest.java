package com.brandnewdata.mop.poc.process.manager;

import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ZeebeClientManagerTest {

    @Autowired
    private ZeebeClientManager manager;

    @Test
    void test() {
        ZeebeClient byEnvId = manager.getByEnvId(1L);
    }

}