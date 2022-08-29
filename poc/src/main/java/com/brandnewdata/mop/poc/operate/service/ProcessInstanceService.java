package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.lang.Assert;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dao.FlowNodeInstanceDao;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dao.SequenceFlowDao;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDTO;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDTO;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDTO;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.entity.SequenceFlowEntity;
import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.SequenceFlowTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDTO;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ProcessInstanceService {

    @Autowired
    private ListViewDao listViewDao;

    @Autowired
    private SequenceFlowDao sequenceFlowDao;

    @Autowired
    private FlowNodeInstanceDao flowNodeInstanceDao;

    @Resource
    private IProcessDeployService deployService;

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
            return t2.compareTo(t1);
        }).collect(Collectors.toList());

        PageEnhancedUtil.setFirstPageNo(1);
        List<ListViewProcessInstanceDTO> records = PageEnhancedUtil.slice(pageNum, pageSize, processInstanceDTOS);

        return new Page<>(processInstanceDTOS.size(), records);
    }

    public List<SequenceFlowEntity> sequenceFlows(Long processInstanceId) {
        Query query = new Query.Builder()
                .term(t -> t.field(SequenceFlowTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                .build();
        return sequenceFlowDao.scrollAll(query);
    }

    public Map<String, FlowNodeStateDTO> getFlowNodeStateMap(Long processInstanceId) {
        Query query = new Query.Builder()
                .term(t -> t.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                .build();
        List<FlowNodeInstanceEntity> entities = flowNodeInstanceDao.list(query, ElasticsearchUtil.QueryType.ALL);

        List<FlowNodeInstanceDTO> flowNodeInstanceDTOS = entities.stream().map(entity -> {
            FlowNodeInstanceDTO dto = new FlowNodeInstanceDTO();
            return dto.from(entity);
        }).sorted((o1, o2) -> {
            // 先按照flowNodeId排序
            String flowNodeId1 = o1.getFlowNodeId();
            String flowNodeId2 = o2.getFlowNodeId();
            int compare = flowNodeId1.compareTo(flowNodeId2);
            if (compare != 0) {
                return compare;
            }
            // 如果节点id相同，就按照时间排序
            LocalDateTime t1 = o1.getStartDate();
            LocalDateTime t2 = o2.getStartDate();
            return t1.compareTo(t2);
        }).collect(Collectors.toList());


        return null;
    }

}
