package com.brandnewdata.mop.poc.group.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.group.dto.BusinessScene;

/**
 * @author caiwillie
 */
public interface IBusinessSceneService {

    Page<BusinessScene> page(int pageNumber, int pageSize);

    BusinessScene detail(Long id);

}
