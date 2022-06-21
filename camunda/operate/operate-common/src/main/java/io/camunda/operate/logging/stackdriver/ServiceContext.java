package io.camunda.operate.logging.stackdriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
final class ServiceContext {
   @JsonProperty("service")
   private String service;
   @JsonProperty("version")
   private String version;

   public String getService() {
      return this.service;
   }

   public void setService(String service) {
      this.service = service;
   }

   public String getVersion() {
      return this.version;
   }

   public void setVersion(String version) {
      this.version = version;
   }
}
