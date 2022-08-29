package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.PageUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dto.GroupDeployDTO;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDTO;
import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDTO;
import com.brandnewdata.mop.poc.process.dto.ProcessInstanceDTO;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
    public Page<ProcessInstanceDTO> pageOld(long deployId, int pageNum, int pageSize) {

        ProcessDeployDTO processDeployDTO = deployService.getOne(deployId);
        Assert.notNull(processDeployDTO, ErrorMessage.NOT_NULL("部署 id"));

        processDeployDTO.getZeebeKey();

        ProcessInstanceFilter instanceFilter = new ProcessInstanceFilter.Builder()
                .bpmnProcessId(ProcessUtil.convertProcessId(processDeployDTO.getProcessId())).build();
        SearchQuery instanceQuery = new SearchQuery.Builder().withFilter(instanceFilter)
                .withSize(100).withSort(new Sort("state", SortOrder.ASC)).build();
        List<io.camunda.operate.dto.ProcessInstance> processInstances = client.searchProcessInstances(instanceQuery);
        processInstances = Optional.ofNullable(processInstances).orElse(ListUtil.empty());

        PageEnhancedUtil.setFirstPageNo(1);
        List<ProcessInstanceDTO> list = PageEnhancedUtil.slice(pageNum, pageSize, processInstances, this::toDTO);
        return new Page<>(processInstances.size(), list);
    }


    public Page<ListViewProcessInstanceDTO> page(Long deployId, Integer pageNum, Integer pageSize) {
        Assert.notNull(deployId);
        Assert.notNull(pageNum);
        Assert.notNull(pageSize);

        ProcessDeployDTO deployDTO = deployService.getOne(deployId);

        Long zeebeKey = deployDTO.getZeebeKey();

        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build(), new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.PROCESS_KEY).value(zeebeKey))
                                .build())
                        .build())
                .build();

        List<ProcessInstanceForListViewEntity> processInstanceForListViewEntities = listViewDao.scrollAll(query);

        List<ListViewProcessInstanceDTO> processInstanceDTOS = processInstanceForListViewEntities.stream().map(entity -> {
            ListViewProcessInstanceDTO dto = new ListViewProcessInstanceDTO();
            dto.from(entity);
            return dto;
        }).sorted((o1, o2) -> {
            LocalDateTime t1 = Optional.ofNullable(o1.getStartDate()).orElse(LocalDateTime.MIN);
            LocalDateTime t2 = Optional.ofNullable(o2.getStartDate()).orElse(LocalDateTime.MIN);
            return t1.compareTo(t2);
        }).collect(Collectors.toList());

        PageEnhancedUtil.setFirstPageNo(1);
        List<ListViewProcessInstanceDTO> records = PageEnhancedUtil.slice(pageNum, pageSize, processInstanceDTOS);

        return new Page<>(processInstanceDTOS.size(), records);
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
