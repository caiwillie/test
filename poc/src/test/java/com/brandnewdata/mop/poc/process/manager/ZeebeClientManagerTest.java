package com.brandnewdata.mop.poc.process.manager;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.BrokerInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ZeebeClientManagerTest {

    @Autowired
    private ZeebeClientManager manager;

    @Test
    void test() {
        ZeebeClient client = manager.getByEnvId(1L);
        List<BrokerInfo> brokers = client.newTopologyRequest().send().join().getBrokers();
        return;
    }

}