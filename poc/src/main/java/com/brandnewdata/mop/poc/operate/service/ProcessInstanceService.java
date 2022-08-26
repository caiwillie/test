package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.PageUtil;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDTO;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDTO;
import com.brandnewdata.mop.poc.process.dto.ProcessInstanceDTO;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.SimpleAuthentication;
import io.camunda.operate.search.ProcessInstanceFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.Sort;
import io.camunda.operate.search.SortOrder;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class ProcessInstanceService {

    @Autowired
    private ListViewDao listViewDao;

    @Value("${brandnewdata.zeebe.operate.uri}")
    private String uri;

    private CamundaOperateClient client;

    @Resource
    private IProcessDeployService deployService;



    @PostConstruct
    @SneakyThrows
    private void init() {
        SimpleAuthentication sa = new SimpleAuthentication("demo", "demo", uri);
        client = new CamundaOperateClient.Builder().operateUrl(uri).authentication(sa).build();
    }

    @SneakyThrows
    public Page<ProcessInstanceDTO> page(long deployId, int pageNum, int pageSize) {

        ProcessDeployDTO processDeployDTO = deployService.getOne(deployId);
        Assert.notNull(processDeployDTO, ErrorMessage.NOT_NULL("部署 id"));

        processDeployDTO.getZeebeKey();

        ProcessInstanceFilter instanceFilter = new ProcessInstanceFilter.Builder()
                .bpmnProcessId(ProcessUtil.convertProcessId(processDeployDTO.getProcessId())).build();
        SearchQuery instanceQuery = new SearchQuery.Builder().withFilter(instanceFilter)
                .withSize(100).withSort(new Sort("state", SortOrder.ASC)).build();
        List<io.camunda.operate.dto.ProcessInstance> processInstances = client.searchProcessInstances(instanceQuery);
        processInstances = Optional.ofNullable(processInstances).orElse(ListUtil.empty());
        List<ProcessInstanceDTO> list = new ArrayList<>();
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


    public Page<ListViewProcessInstanceDTO> pageNew(Long deployId, Integer pageNum, Integer pageSize) {
        Assert.notNull(deployId);
        Assert.notNull(pageNum);
        Assert.notNull(pageSize);

        ProcessDeployDTO deployDTO = deployService.getOne(deployId);
        // Long zeebeKey = deployDTO.getZeebeKey();
        return null;

    }

    private ProcessInstanceDTO toDTO(io.camunda.operate.dto.ProcessInstance processInstance) {
        ProcessInstanceDTO dto = new ProcessInstanceDTO();
        dto.setProcessId(processInstance.getBpmnProcessId());
        dto.setInstanceId(processInstance.getKey());
        dto.setParentInstanceId(processInstance.getParentKey());
        dto.setVersion(processInstance.getProcessVersion());
        dto.setStartTime(DateUtil.formatTime(processInstance.getStartDate()));
        dto.setEndTime(processInstance.getEndDate() != null ?
                DateUtil.formatTime(processInstance.getEndDate()) : null);
        dto.setState(processInstance.getState().name());
        return dto;
    }

}
