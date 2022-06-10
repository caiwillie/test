package com.brandnewdata.mop.modeler.service;

import com.brandnewdata.mop.modeler.dao.DeModelDao;
import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
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
    void testStart() {
        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress("127.0.0.1:26500")
                .usePlaintext()
                .build()) {
            // Setup
            when(mockZeebe.newCreateInstanceCommand()).thenReturn(client.newCreateInstanceCommand());

            // Run the test
            modelServiceUnderTest.start("camunda-cloud-quick-start-advanced");

            // Verify the results
        }
    }

    @Test
    void testDeploy() {


        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress("127.0.0.1:26500")
                .usePlaintext()
                .build()) {
            // Setup
            when(mockZeebe.newDeployResourceCommand()).thenReturn(client.newDeployResourceCommand());

            // Run the test
            modelServiceUnderTest.deploy("gettingstarted_quickstart.bpmn");

            // Verify the results
        }









    }
}
