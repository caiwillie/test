package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.cache.ProcessInstanceNoExpCache;
import com.brandnewdata.mop.poc.operate.dao.FlowNodeInstanceDao;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dao.SequenceFlowDao;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDto;
import com.brandnewdata.mop.poc.operate.dto.ListViewProcessInstanceDto;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.entity.SequenceFlowEntity;
import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.SequenceFlowTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import io.camunda.operate.dto.ProcessInstanceState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ProcessInstanceService implements IProcessInstanceService {

    @Autowired
    private ListViewDao listViewDao;

    @Autowired
    private SequenceFlowDao sequenceFlowDao;

    @Autowired
    private FlowNodeInstanceDao flowNodeInstanceDao;

    @Resource
    private IProcessDeployService deployService;

    @Resource
    private ProcessInstanceNoExpCache processInstanceNoExpCache;

    public Page<ListViewProcessInstanceDto> page(Long deployId, Integer pageNum, Integer pageSize) {
        Assert.notNull(deployId);
        Assert.notNull(pageNum);
        Assert.notNull(pageSize);

        ProcessDeployDto deployDTO = deployService.getOne(deployId);

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

        List<ProcessInstanceForListViewEntity> processInstanceForListViewEntities = listViewDao.scrollAll(query, ElasticsearchUtil.QueryType.ALL);

        List<ListViewProcessInstanceDto> processInstanceDTOS = processInstanceForListViewEntities.stream().map(entity -> {
            ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
            dto.from(entity);
            return dto;
        }).sorted((o1, o2) -> {
            LocalDateTime t1 = Optional.ofNullable(o1.getStartDate()).orElse(LocalDateTime.MIN);
            LocalDateTime t2 = Optional.ofNullable(o2.getStartDate()).orElse(LocalDateTime.MIN);
            return t2.compareTo(t1);
        }).collect(Collectors.toList());

        PageEnhancedUtil.setFirstPageNo(1);
        List<ListViewProcessInstanceDto> records = PageEnhancedUtil.slice(pageNum, pageSize, processInstanceDTOS);

        return new Page<>(processInstanceDTOS.size(), records);
    }

    public ListViewProcessInstanceDto detail(Long processInstanceId) {
        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build(), new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                                .build())
                        .build())
                .build();
        ProcessInstanceForListViewEntity entity = listViewDao.searchOne(query);
        ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
        return dto.from(entity);
    }

    public List<SequenceFlowEntity> sequenceFlows(Long processInstanceId) {
        Query query = new Query.Builder()
                .term(t -> t.field(SequenceFlowTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                .build();
        return sequenceFlowDao.scrollAll(query);
    }

    public Map<String, FlowNodeStateDto> getFlowNodeStateMap(Long processInstanceId) {
        Map<String, FlowNodeStateDto> ret = new HashMap<>();

        Query query = new Query.Builder()
                .term(t -> t.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                .build();
        List<FlowNodeInstanceEntity> entities = flowNodeInstanceDao.list(query, ElasticsearchUtil.QueryType.ALL);

        List<FlowNodeInstanceDto> flowNodeInstanceDTOS = entities.stream().map(entity -> {
            FlowNodeInstanceDto dto = new FlowNodeInstanceDto();
            return dto.from(entity);
        }).sorted((o1, o2) -> {
            int compare = 0;
            // 先按照level倒序排列
            int level1 = o1.getLevel();
            int level2 = o2.getLevel();
            compare = Integer.compare(level2, level1);
            if (compare != 0) return compare;

            // 再按照flowNodeId排序
            String flowNodeId1 = o1.getFlowNodeId();
            String flowNodeId2 = o2.getFlowNodeId();
            compare = flowNodeId1.compareTo(flowNodeId2);
            if (compare != 0) return compare;
            // 如果节点id相同，就按照时间排序
            LocalDateTime t1 = o1.getStartDate();
            LocalDateTime t2 = o2.getStartDate();
            return t1.compareTo(t2);
        }).collect(Collectors.toList());

        Set<String> incidentPathSet = new HashSet<>();
        for (FlowNodeInstanceDto flowNodeInstanceDTO : flowNodeInstanceDTOS) {
            // level 从高往低遍历
            String flowNodeId = flowNodeInstanceDTO.getFlowNodeId();
            String treePath = flowNodeInstanceDTO.getTreePath();
            boolean incident = flowNodeInstanceDTO.isIncident();
            FlowNodeStateDto state = flowNodeInstanceDTO.getState();
            if (incident) {
                state = FlowNodeStateDto.INCIDENT;
                // 将父路径也放入异常路径中（异常冒泡）
                String[] split = treePath.split("/");
                for (int i = 1; i <= split.length; i++) {
                    incidentPathSet.add(StringUtils.join(split, '/', 0, i));
                }
            } else if (incidentPathSet.contains(treePath)) {
                state = FlowNodeStateDto.INCIDENT;
            }
            ret.put(flowNodeId, state);
        }

        return ret;
    }

    public Map<Long, List<ListViewProcessInstanceDto>> listByProcessDefinitionKeyList(List<Long> keys) {
        Map<Long, List<ListViewProcessInstanceDto>> ret = new HashMap<>();
        if (CollUtil.isEmpty(keys)) {
            return ret;
        }

        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build(), new Query.Builder()

                                .terms(new TermsQuery.Builder()
                                        .field(ListViewTemplate.PROCESS_KEY)
                                        .terms(new TermsQueryField.Builder()
                                                .value(keys.stream().map(FieldValue::of).collect(Collectors.toList()))
                                                .build())
                                        .build())
                                .build(), new Query.Builder()

                                // 添加 activityState = active 的条件
                                .term(t -> t.field(ListViewTemplate.STATE).value(ProcessInstanceState.ACTIVE.name()))
                                .build())
                        .build())
                .build();

        List<ProcessInstanceForListViewEntity> entities = listViewDao.scrollAll(query, ElasticsearchUtil.QueryType.ONLY_RUNTIME);

        for (ProcessInstanceForListViewEntity entity : entities) {
            // 转换为 list dto
            ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
            dto.from(entity);

            Long processId = dto.getProcessId();
            List<ListViewProcessInstanceDto> dtos = ret.computeIfAbsent(processId, k -> new ArrayList<>());

            dtos.add(dto);
        }
        return ret;
    }

    @Override
    public Page<ListViewProcessInstanceDto> pageProcessInstanceByZeebeKey(
            List<Long> zeebeKeyList, int pageNum, int pageSize, Map<String, Object> extra) {
        if(CollUtil.isEmpty(zeebeKeyList)) return new Page<>(0, ListUtil.empty());

        List<ListViewProcessInstanceDto> processInstanceDtoList = listProcessInstanceByZeebeKey(zeebeKeyList);

        PageEnhancedUtil.setFirstPageNo(1);
        List<ListViewProcessInstanceDto> records = PageEnhancedUtil.slice(pageNum, pageSize, processInstanceDtoList);

        Page<ListViewProcessInstanceDto> page = new Page<>(processInstanceDtoList.size(), records);
        Map<String, Object> extraMap = new HashMap<>();
        extraMap.put("successCount", 1);
        extraMap.put("failCount", 1);
        extraMap.put("activeCount", 1);
        extraMap.put("cancleCount", 1);
        page.setExtraMap(extraMap);
        return page;
    }

    @Override
    public List<ListViewProcessInstanceDto> listProcessInstanceByZeebeKey(List<Long> zeebeKeyList) {
        if(CollUtil.isEmpty(zeebeKeyList)) return ListUtil.empty();

        List<FieldValue> values = zeebeKeyList.stream().map(key -> new FieldValue.Builder().longValue(key).build())
                .collect(Collectors.toList());

        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build(), new Query.Builder()
                                .terms(new TermsQuery.Builder()
                                        .field(ListViewTemplate.PROCESS_KEY)
                                        .terms(new TermsQueryField.Builder().value(values).build())
                                        .build())
                                .build())
                        .build())
                .build();

        List<ProcessInstanceForListViewEntity> processInstanceForListViewEntities = listViewDao.scrollAll(query, ElasticsearchUtil.QueryType.ALL);

        return processInstanceForListViewEntities.stream().map(entity -> {
            ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
            dto.from(entity);
            return dto;
        }).sorted((o1, o2) -> {
            LocalDateTime t1 = Optional.ofNullable(o1.getStartDate()).orElse(LocalDateTime.MIN);
            LocalDateTime t2 = Optional.ofNullable(o2.getStartDate()).orElse(LocalDateTime.MIN);
            return t2.compareTo(t1);
        }).collect(Collectors.toList());
    }


    @Override
    public List<ListViewProcessInstanceDto> listAll() {
        return ListUtil.toList(processInstanceNoExpCache.asMap().values());
    }


}
