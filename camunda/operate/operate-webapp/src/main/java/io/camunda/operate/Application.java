/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.Application$ApplicationErrorListener
 *  io.camunda.operate.data.DataGenerator
 *  io.camunda.operate.webapp.security.OperateProfileService
 *  org.springframework.boot.SpringApplication
 *  org.springframework.boot.autoconfigure.EnableAutoConfiguration
 *  org.springframework.boot.autoconfigure.SpringBootApplication
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.context.ApplicationContextInitializer
 *  org.springframework.context.ApplicationListener
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.ComponentScan
 *  org.springframework.context.annotation.ComponentScan$Filter
 *  org.springframework.context.annotation.FilterType
 *  org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator
 *  org.springframework.core.env.ConfigurableEnvironment
 */
package io.camunda.operate;

import io.camunda.operate.Application;
import io.camunda.operate.data.DataGenerator;
import io.camunda.operate.webapp.security.OperateProfileService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
@ComponentScan(basePackages={"io.camunda.operate"}, excludeFilters={@ComponentScan.Filter(type=FilterType.REGEX, pattern={"io\\.camunda\\.operate\\.zeebeimport\\..*"}), @ComponentScan.Filter(type=FilterType.REGEX, pattern={"io\\.camunda\\.operate\\.webapp\\..*"}), @ComponentScan.Filter(type=FilterType.REGEX, pattern={"io\\.camunda\\.operate\\.archiver\\..*"})}, nameGenerator=FullyQualifiedAnnotationBeanNameGenerator.class)
@EnableAutoConfiguration
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    public static final String SPRING_THYMELEAF_PREFIX_KEY = "spring.thymeleaf.prefix";
    public static final String SPRING_THYMELEAF_PREFIX_VALUE = "classpath:/META-INF/resources/";

    public static void main(String[] args) {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        System.setProperty("spring.config.location", "optional:classpath:/,optional:classpath:/config/,optional:file:./,optional:file:./config/");
        SpringApplication springApplication = new SpringApplication(new Class[]{Application.class});
        springApplication.setLazyInitialization(true);
        springApplication.setAddCommandLineProperties(true);
        springApplication.addListeners(new ApplicationListener[]{new ApplicationErrorListener()});
        Application.setDefaultProperties(springApplication);
        Application.setDefaultAuthProfile(springApplication);
        springApplication.run(args);
    }

    private static void setDefaultAuthProfile(SpringApplication springApplication) {
        springApplication.addInitializers(new ApplicationContextInitializer[]{configurableApplicationContext -> {
            ConfigurableEnvironment env = configurableApplicationContext.getEnvironment();
            Set<String> activeProfiles = Set.of(env.getActiveProfiles());
            if (!OperateProfileService.AUTH_PROFILES.stream().noneMatch(activeProfiles::contains)) return;
            env.addActiveProfile("auth");
        }});
    }

    private static void setDefaultProperties(SpringApplication springApplication) {
        HashMap<String, Object> defaultsProperties = new HashMap<String, Object>();
        defaultsProperties.putAll(Application.getWebProperties());
        defaultsProperties.putAll(Application.getManagementProperties());
        springApplication.setDefaultProperties(defaultsProperties);
    }

    private static Map<String, Object> getWebProperties() {
        return Map.of("server.servlet.session.cookie.name", "OPERATE-SESSION", SPRING_THYMELEAF_PREFIX_KEY, SPRING_THYMELEAF_PREFIX_VALUE);
    }

    public static Map<String, Object> getManagementProperties() {
        return Map.of("management.health.defaults.enabled", "false", "management.endpoint.health.probes.enabled", "true", "management.endpoints.web.exposure.include", "health, prometheus, loggers, usage-metrics", "management.endpoint.health.group.readiness.include", "readinessState,elsIndicesCheck");
    }

    @Bean(name={"dataGenerator"})
    @ConditionalOnMissingBean
    public DataGenerator stubDataGenerator() {
        logger.debug("Create Data generator stub");
        return DataGenerator.DO_NOTHING;
    }

    public static class ApplicationErrorListener implements ApplicationListener<ApplicationFailedEvent> {
        public void onApplicationEvent(ApplicationFailedEvent event) {
            event.getApplicationContext().close();
            System.exit(-1);
        }
    }
}
