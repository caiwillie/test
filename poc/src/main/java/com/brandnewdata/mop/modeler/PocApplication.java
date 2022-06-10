package com.brandnewdata.mop.modeler;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// https://github.com/camunda-community-hub/spring-zeebe/
@EnableZeebeClient

@SpringBootApplication
public class PocApplication {
	public static void main(String[] args) {
		SpringApplication.run(PocApplication.class, args);
	}

}
