package com.brandnewdata.mop.poc.env.service;

import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.dto.EnvServiceDto;

import java.util.List;

public interface IEnvService {

    EnvDto getOne(Long envId);
    List<EnvDto> listEnv();

    List<EnvServiceDto> listEnvService(Long envId);


}
