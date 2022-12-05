package com.brandnewdata.mop.poc.bff.converter.env;

import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import com.brandnewdata.mop.poc.env.dto.EnvDto;

public class EnvVoConverter {
    public static EnvVo createFrom(EnvDto envDto) {
        EnvVo envVo = new EnvVo();
        envVo.setId(envDto.getId());
        envVo.setCreateTime(envDto.getCreateTime());
        envVo.setUpdateTime(envDto.getUpdateTime());
        envVo.setDeployTime(envDto.getDeployTime());
        envVo.setName(envDto.getName());
        envVo.setStatus(envDto.getStatus());
        envVo.setDescription(envDto.getDescription());
        return envVo;
    }

}
