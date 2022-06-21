package io.camunda.operate.webapp.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientConfigRestService {
   public static final String CLIENT_CONFIG_RESOURCE = "/client-config.js";
   @Autowired
   private ClientConfig clientConfig;

   @GetMapping(
      path = {"/client-config.js"},
      produces = {"text/javascript"}
   )
   public String getClientConfig() {
      return this.clientConfig.asJson();
   }
}
