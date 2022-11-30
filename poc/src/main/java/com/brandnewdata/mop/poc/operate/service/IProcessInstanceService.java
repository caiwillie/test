package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;

import java.util.List;
import java.util.Map;

public interface IProcessInstanceService {

    Page<ListViewProcessInstanceDto> pageProcessInstanceByZeebeKey(
            Long envId,
            List<Long> zeebeKeyList,
            int pageNum,
            int pageSize,
            Map<String, Object> extraMap);

    List<ListViewProcessInstanceDto> listProcessInstanceByZeebeKey(
            Long envId,
            List<Long> zeebeKeyList);

    List<ListViewProcessInstanceDto> listAll();
}
