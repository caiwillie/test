package com.brandnewdata.mop.poc.env.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "brandnewdata.cloud-native")
public class CloudNativeConfigure {
    private Map<String, String> debugServicePort;

    @PostConstruct
    void test() {
        return;
    }
}
