package com.brandnewdata.mop.poc.env.service;

import com.brandnewdata.mop.poc.env.dto.EnvDto;
import com.brandnewdata.mop.poc.env.dto.EnvServiceDto;

import java.util.List;

public interface IEnvService {

    EnvDto fetchOne(Long envId);

    List<EnvDto> fetchEnvList();

    List<EnvServiceDto> fetchEnvService(Long envId);

    EnvDto fetchDebugEnv();


}
