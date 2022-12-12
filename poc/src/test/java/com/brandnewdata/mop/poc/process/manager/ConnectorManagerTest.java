package com.brandnewdata.mop.poc.process.manager;

import com.brandnewdata.mop.poc.process.manager.dto.ConfigInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ConnectorManagerTest {

    @Resource
    private ConnectorManager connectorManager;

    @Test
    void test() {
        ConfigInfo ret = connectorManager.getConfigInfo("1580036578698813441");
        return;
    }

}