package com.brandnewdata.mop.poc.process.cache;

import com.brandnewdata.mop.poc.env.service.IEnvService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class EnvNoExpCache {

    @Resource
    private IEnvService envService;

    private long lastId = 0;


}
