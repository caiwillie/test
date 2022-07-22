package com.brandnewdata.mop.poc.group.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.group.dto.BusinessScene;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;

/**
 * @author caiwillie
 */
public interface IBusinessSceneService {

    Page<BusinessScene> page(int pageNumber, int pageSize);

    BusinessScene detail(Long id);

    BusinessScene save(BusinessScene businessScene);

    ProcessDefinition saveProcessDefinition(Long businessScene, ProcessDefinition processDefinition);

}
