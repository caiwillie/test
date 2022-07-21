package com.brandnewdata.mop.poc.group.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.group.dto.BusinessScene;
import org.springframework.stereotype.Service;

/**
 * @author caiwillie
 */
@Service
public class BusinessSceneService implements IBusinessSceneService {


    @Override
    public Page<BusinessScene> page(int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public BusinessScene detail(Long id) {
        return null;
    }
}
