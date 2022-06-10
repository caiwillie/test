package com.brandnewdata.mop.poc.service;


import com.brandnewdata.mop.poc.dao.DeModelDao;
import com.brandnewdata.mop.poc.pojo.entity.DeModelEntity;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Value;
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

    @Resource
    private ZeebeClient zeebe;


    @Value("${brandnewdata.datasource.url}")
    private String url;

    @PostConstruct
    void post() {
        return;
    }

    public void add(DeModelEntity entity) {
        int insert = modelDao.insert(entity);
        return;
    }

    public void deploy(String classPath) {
        DeploymentEvent deploymentEvent =
                zeebe.newDeployResourceCommand()
                        .addResourceFromClasspath(classPath)
                        .send()
                        .join();
        return;
    }

    public void start(String processId) {
        // camunda-cloud-quick-start-advanced

        ProcessInstanceEvent processInstanceEvent =
                zeebe
                        .newCreateInstanceCommand()
                        .bpmnProcessId(processId)
                        .latestVersion()
                        .send()
                        .join();

        return;
    }
}
