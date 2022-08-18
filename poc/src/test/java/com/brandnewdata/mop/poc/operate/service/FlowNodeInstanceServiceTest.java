package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlowNodeInstanceServiceTest {

    @Autowired
    private FlowNodeInstanceService service;


    @Test
    void list() {
        FlowNodeInstanceRequest request = new FlowNodeInstanceRequest();
        request.setProcessInstanceId("2251799813685305");
        service.list(request);
    }
}