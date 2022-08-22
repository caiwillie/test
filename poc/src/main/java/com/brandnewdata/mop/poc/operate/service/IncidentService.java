package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.mop.poc.operate.dao.IncidentDao;
import com.brandnewdata.mop.poc.operate.dto.IncidentDto;
import com.brandnewdata.mop.poc.operate.entity.IncidentEntity;
import com.brandnewdata.mop.poc.operate.util.TreePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author caiwillie
 */
@Service
public class IncidentService {
    @Autowired
    private IncidentDao incidentDao;

    public IncidentDto getOneByFlowNodeInstance(String processInstanceId, String flowNodeId, String flowNodeInstanceId) {
        IncidentDto ret = new IncidentDto();
        Assert.notNull(processInstanceId);
        Assert.notNull(flowNodeId);
        Assert.notNull(flowNodeInstanceId);

        return null;
    }

}
