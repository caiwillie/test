package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.bo.StatisticCountBo;
import com.brandnewdata.mop.poc.operate.cache.ProcessInstanceCache;
import com.brandnewdata.mop.poc.operate.converter.SequenceFlowDtoConverter;
import com.brandnewdata.mop.poc.operate.dao.FlowNodeInstanceDao;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dao.SequenceFlowDao;
import com.brandnewdata.mop.poc.operate.dto.*;
import com.brandnewdata.mop.poc.operate.dto.filter.ProcessInstanceFilter;
import com.brandnewdata.mop.poc.operate.dto.statistic.ProcessInstanceKeyAgg;
import com.brandnewdata.mop.poc.operate.dto.statistic.ProcessInstanceStateAgg;
import com.brandnewdata.mop.poc.operate.manager.DaoManager;
import com.brandnewdata.mop.poc.operate.po.FlowNodeInstancePo;
import com.brandnewdata.mop.poc.operate.po.SequenceFlowPo;
import com.brandnewdata.mop.poc.operate.po.listview.ProcessInstanceForListViewPo;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.SequenceFlowTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import io.camunda.operate.dto.ProcessInstanceState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessInstanceService implements IProcessInstanceService {

    private final DaoManager daoManager;

    private final ProcessInstanceCache processInstanceCache;

    private static final ZoneOffset DEFAULT_ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public ProcessInstanceService(DaoManager daoManager, ProcessInstanceCache processInstanceCache) {
        this.daoManager = daoManager;
        this.processInstanceCache = processInstanceCache;
    }

    @Override
    public Page<ListViewProcessInstanceDto> pageProcessInstanceByZeebeKey(Long envId,
                                                                          List<Long> zeebeKeyList,
                                                                          int pageNum,
                                                                          int pageSize,
                                                                          ProcessInstanceFilter filter,
                                                                          Map<String, Object> extra) {
        Assert.notNull(envId, "envId is null");
        if(CollUtil.isEmpty(zeebeKeyList)) return new Page<>(0, ListUtil.empty());
        Assert.isFalse(CollUtil.hasNull(zeebeKeyList), "zeebeKeyList has null");
        Query query = assembleQuery(zeebeKeyList, filter);

        int from = (pageNum - 1) * pageSize;

        ListViewDao listViewDao = daoManager.getListViewDaoByEnvId(envId);

        SortOptions sortOption = new SortOptions.Builder()
                .field(new FieldSort.Builder().field(ListViewTemplate.START_DATE).order(SortOrder.Desc).build())
                .build();
        List<ProcessInstanceForListViewPo> listViewPoList =
                listViewDao.searchList(query, from, pageSize, ListUtil.of(sortOption), ElasticsearchUtil.QueryType.ALL);

        List<ListViewProcessInstanceDto> records = listViewPoList.stream().map(entity -> {
            ListViewProcessInstanceDto dto = new ListViewProcessInstanceDto();
            dto.from(entity);
            return dto;
        }).collect(Collectors.toList());

        List<ProcessInstanceStateAgg> processInstanceStateAggs = aggProcessInstanceState(envId, zeebeKeyList, filter);

        Map<String, Object> extraMap = new HashMap<>();
        int total = 0;
        for (ProcessInstanceStateAgg agg : processInstanceStateAggs) {
            String state = agg.getState();
            Boolean incident = agg.getIncident();
            Integer docCount = agg.getDocCount();
            total += docCount;
            if(StrUtil.equals(state, ProcessInstanceState.COMPLETED.name())) {
                extraMap.put("successCount", docCount);
            } else if (StrUtil.equals(state, ProcessInstanceState.CANCELED.name())) {
                extraMap.put("cancleCount", docCount);
            } else if (StrUtil.equals(state, ProcessInstanceState.ACTIVE.name()) && incident) {
                extraMap.put("failCount", docCount);
            } else if (StrUtil.equals(state, ProcessInstanceState.ACTIVE.name()) && !incident) {
                extraMap.put("activeCount", docCount);
            }
        }

        Page<ListViewProcessInstanceDto> page = new Page<>(total, records);
        page.setExtraMap(extraMap);
        return page;
    }

    @Override
    public List<ListViewProcessInstanceDto> listProcessInstanceByZeebeKey(Long envId, List<Long> zeebeKeyList, ProcessInstanceFilter filter) {
        if(CollUtil.isEmpty(zeebeKeyList)) return ListUtil.empty();

        Query query = assembleQuery(zeebeKeyList, filter);

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
    public List<ProcessInstanceKeyAgg> aggProcessInstanceKey(Long envId, List<Long> zeebeKeyList, ProcessInstanceFilter filter) {
        List<ProcessInstanceKeyAgg> ret = new ArrayList<>();
        Assert.notNull(envId, "envId is null");
        if(CollUtil.isEmpty(zeebeKeyList)) return ListUtil.empty();
        Assert.isFalse(CollUtil.hasNull(zeebeKeyList), "zeebeKeyList has null");
        Query query = assembleQuery(zeebeKeyList, filter);

        List<Map<String, CompositeAggregationSource>> sourceList = new ArrayList<>();

        CompositeAggregationSource processDefinitionKeySource = new CompositeAggregationSource.Builder()
                .terms(new TermsAggregation.Builder().field(ListViewTemplate.PROCESS_KEY).build())
                .build();
        sourceList.add(MapUtil.of(ListViewTemplate.PROCESS_KEY, processDefinitionKeySource));

        CompositeAggregationSource startDateSource = new CompositeAggregationSource.Builder()
                .dateHistogram(new DateHistogramAggregation.Builder().field(ListViewTemplate.START_DATE)
                        .calendarInterval(CalendarInterval.Day).build())
                .build();
        sourceList.add(MapUtil.of(ListViewTemplate.START_DATE, startDateSource));

        CompositeAggregationSource stateSource = new CompositeAggregationSource.Builder()
                .terms(new TermsAggregation.Builder().field(ListViewTemplate.STATE).build())
                .build();
        sourceList.add(MapUtil.of(ListViewTemplate.STATE, stateSource));

        CompositeAggregationSource incidentSource = new CompositeAggregationSource.Builder()
                .terms(new TermsAggregation.Builder().field(ListViewTemplate.INCIDENT).build())
                .build();
        sourceList.add(MapUtil.of(ListViewTemplate.INCIDENT, incidentSource));

        ListViewDao listViewDao = daoManager.getListViewDaoByEnvId(envId);
        List<CompositeBucket> bucketList = listViewDao.aggregation(query, sourceList, ElasticsearchUtil.QueryType.ALL);
        if(CollUtil.isEmpty(bucketList)) return ret;

        for (CompositeBucket bucket : bucketList) {
            ProcessInstanceKeyAgg processInstanceKeyAgg = new ProcessInstanceKeyAgg();
            long docCount = bucket.docCount();
            Map<String, JsonData> keyMap = bucket.key();

            Long processDefinitionKey = keyMap.get(ListViewTemplate.PROCESS_KEY).to(Long.class);
            Long startDateMillis = keyMap.get(ListViewTemplate.START_DATE).to(Long.class);
            LocalDate startDate = LocalDateTimeUtil.of(Instant.ofEpochMilli(startDateMillis)).toLocalDate();
            String state = keyMap.get(ListViewTemplate.STATE).to(String.class);
            Boolean incident = keyMap.get(ListViewTemplate.INCIDENT).to(Boolean.class);
            processInstanceKeyAgg.setProcessInstanceKey(processDefinitionKey);
            processInstanceKeyAgg.setStartDate(startDate);
            processInstanceKeyAgg.setState(state);
            processInstanceKeyAgg.setIncident(incident);
            processInstanceKeyAgg.setDocCount((int) docCount);
            ret.add(processInstanceKeyAgg);
        }

        return ret;
    }

    @Override
    public List<ProcessInstanceStateAgg> aggProcessInstanceState(Long envId, List<Long> zeebeKeyList, ProcessInstanceFilter filter) {
        List<ProcessInstanceStateAgg> ret = new ArrayList<>();
        Assert.notNull(envId, "envId is null");
        if(CollUtil.isEmpty(zeebeKeyList)) return ListUtil.empty();
        Assert.isFalse(CollUtil.hasNull(zeebeKeyList), "zeebeKeyList has null");
        Query query = assembleQuery(zeebeKeyList, filter);

        List<Map<String, CompositeAggregationSource>> sourceList = new ArrayList<>();
        CompositeAggregationSource stateSource = new CompositeAggregationSource.Builder()
                .terms(new TermsAggregation.Builder().field(ListViewTemplate.STATE).build())
                .build();
        sourceList.add(MapUtil.of(ListViewTemplate.STATE, stateSource));

        CompositeAggregationSource incidentSource = new CompositeAggregationSource.Builder()
                .terms(new TermsAggregation.Builder().field(ListViewTemplate.INCIDENT).build())
                .build();
        sourceList.add(MapUtil.of(ListViewTemplate.INCIDENT, incidentSource));

        ListViewDao listViewDao = daoManager.getListViewDaoByEnvId(envId);
        List<CompositeBucket> bucketList = listViewDao.aggregation(query, sourceList, ElasticsearchUtil.QueryType.ALL);
        if(CollUtil.isEmpty(bucketList)) return ret;

        for (CompositeBucket bucket : bucketList) {
            ProcessInstanceStateAgg processInstanceStateAgg = new ProcessInstanceStateAgg();
            long docCount = bucket.docCount();
            Map<String, JsonData> keyMap = bucket.key();

            String state = keyMap.get(ListViewTemplate.STATE).to(String.class);
            Boolean incident = keyMap.get(ListViewTemplate.INCIDENT).to(Boolean.class);
            processInstanceStateAgg.setState(state);
            processInstanceStateAgg.setIncident(incident);
            processInstanceStateAgg.setDocCount((int) docCount);
            ret.add(processInstanceStateAgg);
        }

        return ret;
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

    private Query assembleQuery(List<Long> zeebeKeyList, ProcessInstanceFilter filter) {
        Assert.notNull(filter);

        Long minStartTime = Opt.ofNullable(filter.getMinStartTime())
                .map(time -> time.toInstant(DEFAULT_ZONE_OFFSET).toEpochMilli()).orElse(null);
        Long maxStartTime = Opt.ofNullable(filter.getMaxStartTime())
                .map(time -> time.toInstant(DEFAULT_ZONE_OFFSET).toEpochMilli()).orElse(null);

        List<FieldValue> zeebeKeyFieldValueList = zeebeKeyList.stream().map(key -> new FieldValue.Builder().longValue(key).build())
                .collect(Collectors.toList());

        List<Query> mustQueryList = new ArrayList<>();
        mustQueryList.add(new Query.Builder()
                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                .build());
        mustQueryList.add(new Query.Builder()
                .terms(new TermsQuery.Builder()
                        .field(ListViewTemplate.PROCESS_KEY)
                        .terms(new TermsQueryField.Builder().value(zeebeKeyFieldValueList).build()).build())
                .build());

        if(minStartTime != null) {
            mustQueryList.add(new Query.Builder()
                    .range(new RangeQuery.Builder().field(ListViewTemplate.START_DATE).gte(JsonData.of(minStartTime)).build())
                    .build());
        }

        if(maxStartTime != null) {
            mustQueryList.add(new Query.Builder()
                    .range(new RangeQuery.Builder().field(ListViewTemplate.START_DATE).lte(JsonData.of(maxStartTime)).build())
                    .build());
        }

        return new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(mustQueryList)
                        .build())
                .build();
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
