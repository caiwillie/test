package com.brandnewdata.mop.poc.service;

import com.brandnewdata.mop.poc.dao.DeModelDao;
import com.brandnewdata.mop.poc.pojo.entity.DeModelEntity;
import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelServiceTest {

    @Mock
    private DeModelDao mockModelDao;
    @Mock
    private ZeebeClient mockZeebe;

    @InjectMocks
    private ModelService modelServiceUnderTest;


    @Test
    void testAdd() {
        // Setup
        final DeModelEntity entity = new DeModelEntity();
        entity.setId(0L);
        entity.setCreateBy(LocalDateTime.of(2020, 1, 1, 0, 0, 0));
        entity.setCreateTime("createTime");
        entity.setUpdateBy("updateBy");
        entity.setUpdateTime(LocalDateTime.of(2020, 1, 1, 0, 0, 0));
        entity.setName("name");
        entity.setModelKey("modelKy");
        entity.setEditorXml("editorJson");

        when(mockModelDao.insert(any(DeModelEntity.class))).thenReturn(0);

        // Run the test


        // Verify the results
    }

    @Test
    void testDeploy() {
        // Setup
        when(mockZeebe.newDeployResourceCommand()).thenReturn(null);

        // Run the test
        modelServiceUnderTest.deploy("classPath");

        // Verify the results
    }

    @Test
    void testStart() {
        // Setup
        when(mockZeebe.newCreateInstanceCommand()).thenReturn(null);

        // Run the test
        modelServiceUnderTest.start("processId");

        // Verify the results
    }
}
