package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.operate.dao.VariableDao;
import com.brandnewdata.mop.poc.operate.dto.VariableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VariableService2 implements IVariableService2 {

    @Autowired
    private VariableDao variableDao;

    @Override
    public List<VariableDto> listByScopeId(String processInstanceId, String scopeId, Long envId) {

        return null;
    }

}
