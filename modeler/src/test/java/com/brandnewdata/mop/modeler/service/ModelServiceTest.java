package com.brandnewdata.mop.modeler.service;

import com.brandnewdata.mop.modeler.dao.DeModelDao;
import com.brandnewdata.mop.modeler.pojo.entity.DeModelEntity;
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

    @InjectMocks
    private ModelService modelServiceUnderTest;

    @Test
    void testAdd() {
        // Setup
        final DeModelEntity entity = new DeModelEntity();
        entity.setId("id");
        entity.setCreatedBy(LocalDateTime.of(2020, 1, 1, 0, 0, 0));
        entity.setCreated("created");
        entity.setLastUpdatedBy("lastUpdatedBy");
        entity.setLastUpdated(LocalDateTime.of(2020, 1, 1, 0, 0, 0));

        when(mockModelDao.insert(any(DeModelEntity.class))).thenReturn(0);

        // Run the test
        modelServiceUnderTest.add(entity);

        // Verify the results
    }
}
