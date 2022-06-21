package io.camunda.operate.util.rest;

import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatefultRestTemplateConfiguration {
   @Value("${server.servlet.context-path:/}")
   private String contextPath;

   @Bean
   public BiFunction<String, Integer, StatefulRestTemplate> statefulRestTemplateFactory() {
      return (host, port) -> {
         return new StatefulRestTemplate(host, port, this.contextPath);
      };
   }
}
