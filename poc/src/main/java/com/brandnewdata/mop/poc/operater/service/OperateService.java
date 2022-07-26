package com.brandnewdata.mop.poc.operater.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.SimpleAuthentication;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.search.ProcessDefinitionFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.Sort;
import io.camunda.operate.search.SortOrder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperateService {

    CamundaOperateClient client;

    {
        SimpleAuthentication sa = new SimpleAuthentication("demo", "demo", "http://10.101.53.4:18081");
        try {
            client = new CamundaOperateClient.Builder().operateUrl("http://10.101.53.4:18081").authentication(sa).build();
        } catch (OperateException e) {
            throw new RuntimeException(e);
        }
    }

    public Page<ProcessDefinition> processDefinitionPage() throws OperateException {
        return null;
    }

}
