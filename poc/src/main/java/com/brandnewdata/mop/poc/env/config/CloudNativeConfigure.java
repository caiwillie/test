package com.brandnewdata.mop.poc.env.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "brandnewdata.cloud-native")
public class CloudNativeConfigure {
    private Map<String, Integer> debugServicePort;
}
