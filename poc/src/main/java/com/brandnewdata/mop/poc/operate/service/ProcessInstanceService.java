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
import com.brandnewdata.mop.poc.operate.bo.StatisticCountBo;
import com.brandnewdata.mop.poc.operate.cache.ProcessInstanceCache;
import com.brandnewdata.mop.poc.operate.converter.SequenceFlowDtoConverter;
import com.brandnewdata.mop.poc.operate.dao.FlowNodeInstanceDao;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dao.SequenceFlowDao;
import com.brandnewdata.mop.poc.operate.dto.*;
import com.brandnewdata.mop.poc.operate.dto.filter.ProcessInstanceFilter;
import com.brandnewdata.mop.poc.operate.manager.DaoManager;
import com.brandnewdata.mop.poc.operate.po.FlowNodeInstancePo;
import com.brandnewdata.mop.poc.operate.po.SequenceFlowPo;
import com.brandnewdata.mop.poc.operate.po.listview.ProcessInstanceForListViewPo;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.SequenceFlowTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessInstanceService implements IProcessInstanceService {

    private final DaoManager daoManager;

    private final ProcessInstanceCache processInstanceCache;

    public ProcessInstanceService(DaoManager daoManager, ProcessInstanceCache processInstanceCache) {
        this.daoManager = daoManager;
        this.processInstanceCache = processInstanceCache;
    }

    @Override
    public Page<ListViewProcessInstanceDto> pageProcessInstanceByZeebeKey(Long envId,
                                                                          List<Long> zeebeKeyList, int pageNum, int pageSize, Map<String, Object> extra) {
        if(CollUtil.isEmpty(zeebeKeyList)) return new Page<>(0, ListUtil.empty());

        List<ListViewProcessInstanceDto> processInstanceDtoList = listProcessInstanceByZeebeKey(envId, zeebeKeyList);

        StatisticCountBo statisticCountBo = statisticCount(processInstanceDtoList);

        PageEnhancedUtil.setFirstPageNo(1);
        List<ListViewProcessInstanceDto> records = PageEnhancedUtil.slice(pageNum, pageSize, processInstanceDtoList);

        Page<ListViewProcessInstanceDto> page = new Page<>(processInstanceDtoList.size(), records);
        Map<String, Object> extraMap = new HashMap<>();
        extraMap.put("successCount", statisticCountBo.getCompletedCount());
        extraMap.put("failCount", statisticCountBo.getIncidentCount());
        extraMap.put("activeCount", statisticCountBo.getActiveCount());
        extraMap.put("cancleCount", statisticCountBo.getCanceledCount());
        page.setExtraMap(extraMap);
        return page;
    }

    @Override
    public List<ListViewProcessInstanceDto> listProcessInstanceByZeebeKey(Long envId, List<Long> zeebeKeyList) {
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

        ListViewDao listViewDao = daoManager.getListViewDaoByEnvId(envId);
        List<ProcessInstanceForListViewPo> processInstanceForListViewEntities = listViewDao.scrollAll(query, ElasticsearchUtil.QueryType.ALL);

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
    public List<ListViewProcessInstanceDto> listProcessInstanceCacheByZeebeKey(Long envId, List<Long> zeebeKeyList, ProcessInstanceFilter filter) {
        Assert.notNull(envId, "envId is null");
        if(CollUtil.isEmpty(zeebeKeyList)) return ListUtil.empty();
        Assert.isFalse(CollUtil.hasNull(zeebeKeyList), "zeebeKeyList has null");
        Assert.notNull(filter);

        LocalDateTime minStartTime = filter.getMinStartTime();
        LocalDateTime maxStartTime = filter.getMaxStartTime();

        Map<String, ListViewProcessInstanceDto> map = processInstanceCache.asMap(envId);
        return map.values().stream().filter(dto -> {
            if(!zeebeKeyList.contains(dto.getProcessId())) return false;
            LocalDateTime startDate = dto.getStartDate();
            // 最小开始时间，最大开始时间
            if(minStartTime != null && minStartTime.compareTo(startDate) > 0) return false;
            if(maxStartTime != null && maxStartTime.compareTo(startDate) < 0) return false;
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public ListViewProcessInstanceDto detailProcessInstance(Long envId, Long processInstanceId) {

        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build(), new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                                .build())
                        .build())
                .build();


        ListViewDao listViewDao = daoManager.getListViewDaoByEnvId(envId);

        ProcessInstanceForListViewPo entity = listViewDao.searchOne(query);
        ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
        return dto.from(entity);
    }

    @Override
    public List<SequenceFlowDto> sequenceFlows(Long envId, Long processInstanceId) {
        Query query = new Query.Builder()
                .term(t -> t.field(SequenceFlowTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                .build();

        SequenceFlowDao sequenceFlowDao = daoManager.getSequenceFlowDaoByEnvId(envId);
        List<SequenceFlowPo> entities = sequenceFlowDao.scrollAll(query);
        return entities.stream().map(SequenceFlowDtoConverter::createFrom).collect(Collectors.toList());
    }

    @Override
    public Map<String, FlowNodeStateDto> getFlowNodeStateMap(Long envId, Long processInstanceId) {
        Map<String, FlowNodeStateDto> ret = new HashMap<>();

        FlowNodeInstanceDao flowNodeInstanceDao = daoManager.getFlowNodeInstanceDaoByEnvId(envId);

        Query query = new Query.Builder()
                .term(t -> t.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                .build();
        List<FlowNodeInstancePo> entities = flowNodeInstanceDao.list(query, ElasticsearchUtil.QueryType.ALL);

        List<FlowNodeInstanceTreeNodeDto> flowNodeInstanceTreeNodeDTOS = entities.stream().map(entity -> {
            FlowNodeInstanceTreeNodeDto dto = new FlowNodeInstanceTreeNodeDto();
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
        for (FlowNodeInstanceTreeNodeDto flowNodeInstanceTreeNodeDTO : flowNodeInstanceTreeNodeDTOS) {
            // level 从高往低遍历
            String flowNodeId = flowNodeInstanceTreeNodeDTO.getFlowNodeId();
            String treePath = flowNodeInstanceTreeNodeDTO.getTreePath();
            boolean incident = flowNodeInstanceTreeNodeDTO.isIncident();
            FlowNodeStateDto state = flowNodeInstanceTreeNodeDTO.getState();
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

    private StatisticCountBo statisticCount(List<ListViewProcessInstanceDto> processInstanceDtoList) {
        StatisticCountBo ret = new StatisticCountBo();
        int completedCount = 0;
        int activeCount = 0;
        int incidentCount = 0;
        int canceledCount = 0;

        for (ListViewProcessInstanceDto listViewProcessInstanceDto : processInstanceDtoList) {
            ProcessInstanceStateDto state = listViewProcessInstanceDto.getState();
            if(state == ProcessInstanceStateDto.COMPLETED) {
                completedCount++;
            } else if (state == ProcessInstanceStateDto.ACTIVE) {
                activeCount++;
            } else if (state == ProcessInstanceStateDto.INCIDENT) {
                incidentCount++;
            } else if (state == ProcessInstanceStateDto.CANCELED) {
                canceledCount++;
            }
        }
        ret.setCompletedCount(completedCount);
        ret.setActiveCount(activeCount);
        ret.setIncidentCount(incidentCount);
        ret.setCanceledCount(canceledCount);
        return ret;
    }
}
