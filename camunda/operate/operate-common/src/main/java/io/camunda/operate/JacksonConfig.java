package io.camunda.operate;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.operate.es.ElasticsearchConnector;
import io.camunda.operate.property.OperateProperties;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {
   @Autowired
   private OperateProperties operateProperties;

   @Bean({"operateObjectMapper"})
   public ObjectMapper objectMapper() {
      JavaTimeModule javaTimeModule = new JavaTimeModule();
      javaTimeModule.addSerializer(OffsetDateTime.class, new ElasticsearchConnector.CustomOffsetDateTimeSerializer(this.dateTimeFormatter()));
      javaTimeModule.addDeserializer(OffsetDateTime.class, new ElasticsearchConnector.CustomOffsetDateTimeDeserializer(this.dateTimeFormatter()));
      javaTimeModule.addDeserializer(Instant.class, new ElasticsearchConnector.CustomInstantDeserializer());
      return Jackson2ObjectMapperBuilder.json().modules(new Module[]{javaTimeModule, new Jdk8Module()}).featuresToDisable(new Object[]{SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES}).featuresToEnable(new Object[]{Feature.ALLOW_COMMENTS, SerializationFeature.INDENT_OUTPUT}).visibility(PropertyAccessor.GETTER, Visibility.ANY).visibility(PropertyAccessor.IS_GETTER, Visibility.ANY).visibility(PropertyAccessor.SETTER, Visibility.ANY).visibility(PropertyAccessor.FIELD, Visibility.NONE).visibility(PropertyAccessor.CREATOR, Visibility.NONE).build();
   }

   @Bean
   public DateTimeFormatter dateTimeFormatter() {
      return DateTimeFormatter.ofPattern(this.operateProperties.getElasticsearch().getDateFormat());
   }
}
