package com.brandnewdata.mop.poc;

import com.brandnewdata.common.annotation.EnableGlobalExceptionHandler;
import com.brandnewdata.common.annotation.EnableRequestInterceptorComponent;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = ElasticsearchRestClientAutoConfiguration.class)
@ServletComponentScan
@EnableZeebeClient // 开启zeebe https://github.com/camunda-community-hub/spring-zeebe/
@RefreshScope // 开启配置中心自动刷新 https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html
@EnableDiscoveryClient // 开启注册发现 https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html
@EnableFeignClients(basePackages = {/*"com.brandnewdata.connector.api",*/ "com.brandnewdata.mop.api"})
@EnableGlobalExceptionHandler
@EnableRequestInterceptorComponent
@EnableScheduling
public class PocApplication {
	public static void main(String[] args) {
		SpringApplication.run(PocApplication.class, args);
	}

}
