package com.brandnewdata.mop.modeler.service;


import com.brandnewdata.mop.modeler.dao.DeModelDao;
import com.brandnewdata.mop.modeler.pojo.entity.DeModelEntity;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author caiwillie
 */
@Service
public class ModelService {

    @Resource
    private DeModelDao modelDao;


    @Autowired
    private ZeebeClient zeebeClient;



    @PostConstruct
    void post() {
        return;
    }

    public void add(DeModelEntity entity) {
        int insert = modelDao.insert(entity);
        return;
    }

}
