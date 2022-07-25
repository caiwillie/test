package com.brandnewdata.mop.poc.group.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.group.dto.BusinessScene;
import com.brandnewdata.mop.poc.group.dto.BusinessSceneProcessDefinition;

/**
 * @author caiwillie
 */
public interface IBusinessSceneService {

    Page<BusinessScene> page(int pageNumber, int pageSize);

    BusinessScene getOne(Long id);

    BusinessScene save(BusinessScene businessScene);

    BusinessSceneProcessDefinition saveProcessDefinition(BusinessSceneProcessDefinition businessSceneProcessDefinition);

}
