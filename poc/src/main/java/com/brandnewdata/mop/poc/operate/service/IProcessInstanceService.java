package com.brandnewdata.mop.poc.operate.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;

import java.util.List;

public interface IProcessInstanceService {

    Page<ListViewProcessInstanceDto> pageProcessInstanceByZeebeKeyList(List<Long> zeebeKeyList, int pageNum, int pageSize);
}
