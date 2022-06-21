package io.camunda.operate;

import io.camunda.operate.data.DataGenerator;
import io.camunda.operate.webapp.security.OperateProfileService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
@ComponentScan(
   basePackages = {"io.camunda.operate"},
   excludeFilters = {@Filter(
   type = FilterType.REGEX,
   pattern = {"io\\.camunda\\.operate\\.zeebeimport\\..*"}
), @Filter(
   type = FilterType.REGEX,
   pattern = {"io\\.camunda\\.operate\\.webapp\\..*"}
), @Filter(
   type = FilterType.REGEX,
   pattern = {"io\\.camunda\\.operate\\.archiver\\..*"}
)},
   nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@EnableAutoConfiguration
public class Application {
   private static final Logger logger = LoggerFactory.getLogger(Application.class);
   public static final String SPRING_THYMELEAF_PREFIX_KEY = "spring.thymeleaf.prefix";
   public static final String SPRING_THYMELEAF_PREFIX_VALUE = "classpath:/META-INF/resources/";

   public static void main(String[] args) {
      System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
      System.setProperty("spring.config.location", "optional:classpath:/,optional:classpath:/config/,optional:file:./,optional:file:./config/");
      SpringApplication springApplication = new SpringApplication(new Class[]{Application.class});
      springApplication.setAddCommandLineProperties(true);
      springApplication.addListeners(new ApplicationListener[]{new ApplicationErrorListener()});
      setDefaultProperties(springApplication);
      setDefaultAuthProfile(springApplication);
      springApplication.run(args);
   }

   private static void setDefaultAuthProfile(SpringApplication springApplication) {
      springApplication.addInitializers(new ApplicationContextInitializer[]{(configurableApplicationContext) -> {
         ConfigurableEnvironment env = configurableApplicationContext.getEnvironment();
         Set activeProfiles = Set.of(env.getActiveProfiles());
         Stream var10000 = OperateProfileService.AUTH_PROFILES.stream();
         Objects.requireNonNull(activeProfiles);
         if (var10000.noneMatch(activeProfiles::contains)) {
            env.addActiveProfile("auth");
         }

      }});
   }

   private static void setDefaultProperties(SpringApplication springApplication) {
      Map defaultsProperties = new HashMap();
      defaultsProperties.putAll(getWebProperties());
      defaultsProperties.putAll(getManagementProperties());
      springApplication.setDefaultProperties(defaultsProperties);
   }

   private static Map getWebProperties() {
      return Map.of("server.servlet.session.cookie.name", "OPERATE-SESSION", "spring.thymeleaf.prefix", "classpath:/META-INF/resources/");
   }

   public static Map getManagementProperties() {
      return Map.of("management.health.defaults.enabled", "false", "management.endpoint.health.probes.enabled", "true", "management.endpoints.web.exposure.include", "health, prometheus, loggers, usage-metrics", "management.endpoint.health.group.readiness.include", "readinessState,elsIndicesCheck");
   }

   @Bean(
      name = {"dataGenerator"}
   )
   @ConditionalOnMissingBean
   public DataGenerator stubDataGenerator() {
      logger.debug("Create Data generator stub");
      return DataGenerator.DO_NOTHING;
   }

   public static class ApplicationErrorListener implements ApplicationListener<ApplicationFailedEvent> {
      @Override
      public void onApplicationEvent(ApplicationFailedEvent event) {
         event.getApplicationContext().close();
         System.exit(-1);
      }
   }
}
