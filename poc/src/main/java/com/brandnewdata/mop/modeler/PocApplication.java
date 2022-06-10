package com.brandnewdata.mop.modeler;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication
@EnableZeebeClient // 开启zeebe https://github.com/camunda-community-hub/spring-zeebe/
@RefreshScope // 开启配置中心自动刷新 https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html
@EnableDiscoveryClient // 开启注册发现 https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html
public class PocApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocApplication.class, args);
	}

}
