package io.camunda.operate.zeebe;

import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.property.ZeebeProperties;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeConnector {
   private static final Logger logger = LoggerFactory.getLogger(ZeebeConnector.class);
   private static final int JOB_WORKER_MAX_JOBS_ACTIVE = 5;
   @Autowired
   private OperateProperties operateProperties;

   @Bean
   public ZeebeClient zeebeClient() {
      return this.newZeebeClient(this.operateProperties.getZeebe());
   }

   public ZeebeClient newZeebeClient(ZeebeProperties zeebeProperties) {
      ZeebeClientBuilder builder = ZeebeClient.newClientBuilder().gatewayAddress(zeebeProperties.getGatewayAddress()).defaultJobWorkerMaxJobsActive(5);
      if (zeebeProperties.isSecure()) {
         builder.caCertificatePath(zeebeProperties.getCertificatePath());
         logger.info("Use TLS connection to zeebe");
      } else {
         builder.usePlaintext();
         logger.info("Use plaintext connection to zeebe");
      }

      return builder.build();
   }
}
