package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.lang.Assert;
import com.brandnewdata.mop.poc.operate.dao.IncidentDao;
import com.brandnewdata.mop.poc.operate.dto.IncidentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author caiwillie
 */
@Service
public class IncidentService {
    @Autowired
    private IncidentDao incidentDao;

    public IncidentDTO getOneByFlowNodeInstance(String processInstanceId, String flowNodeId, String flowNodeInstanceId) {
        IncidentDTO ret = new IncidentDTO();
        Assert.notNull(processInstanceId);
        Assert.notNull(flowNodeId);
        Assert.notNull(flowNodeInstanceId);

        return null;
    }

}
