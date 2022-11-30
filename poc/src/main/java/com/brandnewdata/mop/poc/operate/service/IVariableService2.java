package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.operate.dto.VariableDto;

import java.util.List;

public interface IVariableService2 {

    List<VariableDto> listByScopeId(Long envId, String processInstanceId, String scopeId);
}
