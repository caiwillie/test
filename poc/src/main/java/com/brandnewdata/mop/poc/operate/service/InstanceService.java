package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.PageUtil;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.process.dto.ProcessInstance;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.SimpleAuthentication;
import io.camunda.operate.search.*;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class InstanceService {

    private static final String URL = "http://10.101.53.4:18081";

    private CamundaOperateClient client;

    public InstanceService() {
        init();
    }

    @SneakyThrows
    private void init() {
        SimpleAuthentication sa = new SimpleAuthentication("demo", "demo", URL);
        client = new CamundaOperateClient.Builder().operateUrl(URL).authentication(sa).build();
    }

    @SneakyThrows
    public Page<ProcessInstance> page(int pageNum, int pageSize) {
        ProcessInstanceFilter instanceFilter = new ProcessInstanceFilter.Builder().bpmnProcessId("Process_2875dfe6-b543-4d98-bdba-87b1f8c4d993").build();
        SearchQuery instanceQuery = new SearchQuery.Builder().withFilter(instanceFilter).withSize(20).withSort(new Sort("state", SortOrder.ASC)).build();
        List<io.camunda.operate.dto.ProcessInstance> processInstances = client.searchProcessInstances(instanceQuery);
        processInstances = Optional.ofNullable(processInstances).orElse(ListUtil.empty());
        List<ProcessInstance> list = new ArrayList<>();
        PageUtil.setFirstPageNo(1);
        int[] startEnd = PageUtil.transToStartEnd(pageNum, pageSize);
        int start = startEnd[0];
        int end = startEnd[1];
        int total = processInstances.size();
        for (int i = start; i < total && i < end; i++) {
            list.add(toDTO(processInstances.get(i)));
        }
        return new Page<>(total, list);
    }

    private ProcessInstance toDTO(io.camunda.operate.dto.ProcessInstance processInstance) {
        ProcessInstance dto = new ProcessInstance();
        dto.setProcessId(processInstance.getBpmnProcessId());
        dto.setInstanceId(processInstance.getKey());
        dto.setParentInstanceId(processInstance.getParentKey());
        dto.setVersion(processInstance.getProcessVersion());
        dto.setStartTime(DateUtil.formatTime(processInstance.getStartDate()));
        dto.setEndTime(processInstance.getEndDate() != null ?
                DateUtil.formatTime(processInstance.getEndDate()) : null);
        return dto;
    }

}
