package com.brandnewdata.mop.modeler;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;


@EnableZeebeClient // https://github.com/camunda-community-hub/spring-zeebe/
@RefreshScope // https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html
@SpringBootApplication
public class PocApplication {
	public static void main(String[] args) {
		SpringApplication.run(PocApplication.class, args);
	}

}
