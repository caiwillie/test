package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployVersionDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployVersionEntity;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.SimpleAuthentication;
import io.camunda.operate.exception.OperateException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperateService {

    @Resource
    private ProcessDeployVersionDao processDeployVersionDao;

    private CamundaOperateClient client;

    {
        SimpleAuthentication sa = new SimpleAuthentication("demo", "demo", "http://10.101.53.4:18081");
        try {
            client = new CamundaOperateClient.Builder().operateUrl("http://10.101.53.4:18081").authentication(sa).build();
        } catch (OperateException e) {
            throw new RuntimeException(e);
        }
    }

    public Page<ProcessDeploy> processDefinitionPage(int pageNum, int pageSize) {

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProcessDeployVersionEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize);
        QueryWrapper<ProcessDeployVersionEntity> queryWrapper = new QueryWrapper<>();
        page = processDeployVersionDao.selectPage(page, queryWrapper);
        List<ProcessDeploy> list = new ArrayList<>();

        List<ProcessDeployVersionEntity> records = page.getRecords();

        if(CollUtil.isNotEmpty(records)) {
            for (ProcessDeployVersionEntity record : records) {
                ProcessDeploy dto = toDTO(record);
                list.add(dto);
            }
        }

        return new Page<>(page.getTotal(), list);
    }

    private ProcessDeploy toDTO(ProcessDeployVersionEntity entity) {
        ProcessDeploy dto = new ProcessDeploy();
        dto.setProcessId(entity.getProcessId());
        dto.setProcessName(entity.getProcessName());
        dto.setXml(entity.getProcessXml());
        dto.setVersion(entity.getVersion());
        dto.setType(entity.getType());
        return dto;
    }

}
