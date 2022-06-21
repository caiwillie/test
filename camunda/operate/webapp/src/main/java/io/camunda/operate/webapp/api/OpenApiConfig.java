package io.camunda.operate.webapp.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
   @Bean
   public OpenAPI operateAPI() {
      return (new OpenAPI()).info((new Info()).title("Operate Public API").description("To access active and completed process instances in Operate for monitoring and troubleshooting").contact((new Contact()).url("https://www.camunda.com")).license((new License()).name("License").url("https://docs.camunda.io/docs/reference/licenses/")));
   }

   @Bean
   public GroupedOpenApi apiV1() {
      return this.apiDefinitionFor("v1");
   }

   private GroupedOpenApi apiDefinitionFor(String version) {
      return GroupedOpenApi.builder().group(version).packagesToScan(new String[]{"io.camunda.operate.webapp.api." + version}).pathsToMatch(new String[]{"/" + version + "/**"}).build();
   }
}
