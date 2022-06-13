package com.brandnewdata.mop.poc.message;

import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;

public class MessageResourceTest {

    @Test
    void test() {

        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress("10.101.53.4:26500")
                .usePlaintext()
                .build()) {
            client.newTopologyRequest().send().join();

            client.newPublishMessageCommand()
                    .messageName("create_user")
                    .correlationKey("")
                    .send()
                    .join();
        }


    }
}
