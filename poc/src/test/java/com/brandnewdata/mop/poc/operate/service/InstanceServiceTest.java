package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dto.ProcessInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class InstanceServiceTest {
    InstanceService instanceService = new InstanceService();

    @Test
    void testPage() {
        Page<ProcessInstance> result = instanceService.page(0, 0);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme