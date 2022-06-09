package com.brandnewdata.mop.modeler.service;


import com.brandnewdata.mop.modeler.dao.DeModelDao;
import com.brandnewdata.mop.modeler.pojo.entity.DeModelEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author caiwillie
 */
@Service
public class ModelService {

    @Resource
    private DeModelDao modelDao;

    public void add(DeModelEntity entity) {
        int insert = modelDao.insert(entity);
        return;
    }

}
