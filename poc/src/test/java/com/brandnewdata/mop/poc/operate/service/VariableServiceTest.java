package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.ListUtil;
import com.brandnewdata.mop.poc.operate.dao.VariableDao;
import com.brandnewdata.mop.poc.operate.dto.VariableDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class VariableServiceTest {
    @Mock
    VariableDao variableDao;
    @Mock
    Logger log;
    @InjectMocks
    VariableService variableService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListByScopeId() {
        when(variableDao.list(any())).thenReturn(ListUtil.empty());

        List<VariableDTO> result = variableService.listByScopeId("2251799815100611", "2251799815100611");
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme