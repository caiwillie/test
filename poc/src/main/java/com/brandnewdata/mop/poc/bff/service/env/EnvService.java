package com.brandnewdata.mop.poc.bff.service.env;

import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnvService {

    @Resource
    private com.brandnewdata.mop.poc.env.service.EnvService service;

    public List<EnvVo> list() {
        return service.listEnv().stream().map(envDto -> new EnvVo().from(envDto)).collect(Collectors.toList());
    }

}
