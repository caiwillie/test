package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.brandnewdata.mop.poc.operate.dao.EventDao;
import com.brandnewdata.mop.poc.operate.dao.FlowNodeInstanceDao;
import com.brandnewdata.mop.poc.operate.dao.IncidentDao;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dto.*;
import com.brandnewdata.mop.poc.operate.entity.EventEntity;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeType;
import com.brandnewdata.mop.poc.operate.entity.IncidentEntity;
import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.brandnewdata.mop.poc.operate.util.TreePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brandnewdata.mop.poc.operate.dto.IncidentDto.FALLBACK_PROCESS_DEFINITION_NAME;

/**
 * @author caiwillie
 */
@Service
public class FlowNodeInstanceService {

    @Autowired
    private FlowNodeInstanceDao flowNodeInstanceDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private IncidentDao incidentDao;

    @Autowired
    private ListViewDao listViewDao;

    public List<FlowNodeInstanceListDto> list(String processInstanceId) {
        Assert.notNull(processInstanceId);

        List<FlowNodeInstanceEntity> flowNodeInstanceEntities = flowNodeInstanceDao.list(processInstanceId);

        List<FlowNodeInstanceListDto> flowNodeInstanceListDtos = flowNodeInstanceEntities.stream()
                .map(entity -> new FlowNodeInstanceListDto().fromEntity(entity)).collect(Collectors.toList());

        // 构造 path to instance 的 map
        Map<String, FlowNodeInstanceListDto> pathInstanceMap = flowNodeInstanceListDtos.stream()
                .collect(Collectors.toMap(FlowNodeInstanceListDto::getTreePath, Function.identity()));

        // 转换为 treeNode
        List<TreeNode<String>> treeNodes = flowNodeInstanceListDtos.stream().map(this::toTreeNode).collect(Collectors.toList());

        // 根节点是 process instance id
        List<Tree<String>> children = TreeUtil.build(treeNodes, processInstanceId);

        FlowNodeInstanceTreeDto ret = buildFlowNodeInstanceTree(children, pathInstanceMap);

        return ret.getList();
    }

    private TreeNode<String> toTreeNode(FlowNodeInstanceListDto flowNodeInstanceListDto) {
        // 将 flow node instance 中的 path 最为 id
        String id = flowNodeInstanceListDto.getTreePath();

        // 将类似 START_EVENT_1 作为name
        String name = flowNodeInstanceListDto.getFlowNodeId();

        // 将 id 作为排序字段
        String weight = flowNodeInstanceListDto.getId();

        // 查询 id 中最后一个 /
        int index = id.lastIndexOf('/');
        String parent = null;
        if (index > 0) {
            parent = id.substring(0, index);
        } else {
            throw new RuntimeException("tree path 格式错误");
        }

        return new TreeNode<>(id, parent, name, weight);
    }

    private FlowNodeInstanceTreeDto buildFlowNodeInstanceTree(List<Tree<String>> children, Map<String, FlowNodeInstanceListDto> pathInstanceMap) {
        FlowNodeInstanceTreeDto ret = new FlowNodeInstanceTreeDto();
        List<FlowNodeInstanceListDto> list = new ArrayList<>();
        ret.setList(list);
        ret.setIncident(false);
        if (CollUtil.isEmpty(children)) {
            return ret;
        }

        for (Tree<String> tree : children) {
            FlowNodeInstanceListDto flowNodeInstanceListDto = pathInstanceMap.get(tree.getId());
            if (flowNodeInstanceListDto == null) throw new RuntimeException("path 数据错误");
            // 递归处理
            FlowNodeInstanceTreeDto childrenDto = buildFlowNodeInstanceTree(tree.getChildren(), pathInstanceMap);

            // if()
            flowNodeInstanceListDto.setChildren(childrenDto.getList());

            if (childrenDto.isIncident() && !flowNodeInstanceListDto.isIncident()) {
                // 子节点有错误事件，但是本节点无错误事件，修改本节点的事件和状态
                flowNodeInstanceListDto.setIncident(true);
                flowNodeInstanceListDto.setState(FlowNodeStateDto.INCIDENT);
            }

            if (flowNodeInstanceListDto.isIncident() && !ret.isIncident()) {
                // 本节点有错误事件，但是总体结果无错误事件时，修改总体结果
                ret.setIncident(true);
            }
            list.add(flowNodeInstanceListDto);
        }

        return ret;
    }

    public FlowNodeInstanceDetailDto detailByFlowNodeInstanceId(String processInstanceId, String flowNodeInstanceId) {
        FlowNodeInstanceDetailDto ret = new FlowNodeInstanceDetailDto();

        Assert.notNull(flowNodeInstanceId);

        // 首先查找 instance entity
        FlowNodeInstanceEntity flowNodeInstance = flowNodeInstanceDao.getOne(flowNodeInstanceId);

        // 查找 event
        EventEntity eventEntity = eventDao.getOne(flowNodeInstanceId);

        // 转换 metaData
        FlowNodeInstanceMetaDataDto metaData =
                new FlowNodeInstanceMetaDataDto().fromEntity(flowNodeInstance, eventEntity);

        // 添加 call activity
        addCallActivityMetaData(metaData);

        // todo 添加 business rule

        // 查找 incident
        Object[] incidentRet = searchIncident(flowNodeInstance);
        long incidentCount = (long) incidentRet[0];
        IncidentDto incidentDto = (IncidentDto) incidentRet[1];


        ret.setRepeated(false);
        ret.setMetaData(metaData);
        ret.setIncidentCount(incidentCount);
        ret.setIncident(incidentDto);
        return ret;
    }


    /**
     * 如果是call activity，就查询更多内容
     */
    private void addCallActivityMetaData(FlowNodeInstanceMetaDataDto metaData) {
        if (metaData.getFlowNodeType().equals(FlowNodeType.CALL_ACTIVITY)) {
            ProcessInstanceForListViewEntity entity = listViewDao.getOneByParentFlowNodeInstanceId(metaData.getFlowNodeInstanceId());
            String calledProcessInstanceId = entity.getId();
            String processName = entity.getProcessName();
            if (processName == null) {
                processName = entity.getBpmnProcessId();
            }
            metaData.setCalledProcessInstanceId(calledProcessInstanceId);
            metaData.setCalledProcessDefinitionName(processName);
        }
    }

    private void addBusinessRuleTaskMetaData(FlowNodeInstanceMetaDataDto metaData) {
        if (metaData.getFlowNodeType().equals(FlowNodeType.BUSINESS_RULE_TASK)) {

        }
    }

    private Object[] searchIncident(FlowNodeInstanceEntity flowNodeInstance) {
        Object[] ret = new Object[2];
        IncidentDto incidentDto = new IncidentDto();

        TreePathUtil treePath = new TreePathUtil();
        Long processInstanceId = flowNodeInstance.getProcessInstanceKey();

        treePath.appendProcessInstance(String.valueOf(processInstanceId));
        // 类似 PI_{processInstanceId}
        String firstPath = treePath.toString();

        treePath.appendFlowNode(flowNodeInstance.getFlowNodeId());
        treePath.appendFlowNodeInstance(flowNodeInstance.getId());

        List<IncidentEntity> incidentEntities = incidentDao.listByTreePath(treePath.toString());

        // 获取incident的个数
        long incidentCount = incidentEntities.size();
        ret[0] = incidentCount;

        if (incidentCount != 1) {
            // 如果事件个数不是 1， 就直接返回
            return ret;
        }

        // 查询详情
        IncidentEntity incidentEntity = incidentEntities.get(0);

        incidentDto.fromEntity(incidentEntity);

        Map<String, IncidentDataHolder> incidentDataHolderMap =
                collectFlowNodeDataForPropagatedIncidents(ListUtil.of(incidentEntity), processInstanceId, firstPath);

        IncidentDataHolder dataHolder = incidentDataHolderMap.get(incidentEntity.getId());

        Map<Long, String> processNames = MapUtil.empty();

        if (dataHolder != null && !Objects.equals(incidentDto.getFlowNodeInstanceId(), dataHolder.getFinalFlowNodeInstanceId())) {
            // 如果data holder中的 flowNodeInstanceId 和 incidentDto中不一样，就替换
            incidentDto.setFlowNodeId(dataHolder.getFinalFlowNodeId());
            incidentDto.setFlowNodeInstanceId(dataHolder.getFinalFlowNodeInstanceId());
            ProcessInstanceReferenceDto rootCauseInstance = new ProcessInstanceReferenceDto();
            rootCauseInstance.setInstanceId(String.valueOf(incidentEntity.getProcessInstanceKey()));
            rootCauseInstance.setProcessDefinitionId(String.valueOf(incidentEntity.getProcessDefinitionKey()));
            if (processNames != null && processNames.get(incidentEntity.getProcessDefinitionKey()) != null) {
                rootCauseInstance.setProcessDefinitionName(processNames.get(incidentEntity.getProcessDefinitionKey()));
            } else {
                rootCauseInstance.setProcessDefinitionName(FALLBACK_PROCESS_DEFINITION_NAME);
            }
            incidentDto.setRootCauseInstance(rootCauseInstance);
        }

        ret[1] = incidentDto;
        return ret;
    }

    private Map<String, IncidentDataHolder> collectFlowNodeDataForPropagatedIncidents(
            List<IncidentEntity> incidents, Long processInstanceId, String currentTreePath) {

        HashSet<String> flowNodeInstanceIdsSet = new HashSet<>();
        HashMap<String, IncidentDataHolder> incDatas = new HashMap<>();
        Iterator<IncidentEntity> iterator = incidents.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                // caiwillie 查询结束后，查出最终的 finalFlowNodeId
                if (flowNodeInstanceIdsSet.size() <= 0) return incDatas;
                Map<String, String> flowNodeIdsMap = this.getFlowNodeIds(flowNodeInstanceIdsSet);
                incDatas.values().stream().filter(iData -> iData.getFinalFlowNodeId() == null)
                        .forEach(iData -> iData.setFinalFlowNodeId(flowNodeIdsMap.get(iData.getFinalFlowNodeInstanceId())));
                return incDatas;
            }
            IncidentEntity incident = iterator.next();
            IncidentDataHolder holder = new IncidentDataHolder();
            holder.setIncidentId(incident.getId());
            if (!NumberUtil.equals(incident.getProcessInstanceKey(), processInstanceId)) {
                // 这里时为了获取异常 call activity 的 instance id
                String callActivityInstanceId = TreePathUtil.extractFlowNodeInstanceId(incident.getTreePath(), currentTreePath);
                holder.setFinalFlowNodeInstanceId(callActivityInstanceId);
                flowNodeInstanceIdsSet.add(callActivityInstanceId);
            } else {
                holder.setFinalFlowNodeInstanceId(String.valueOf(incident.getFlowNodeInstanceKey()));
                holder.setFinalFlowNodeId(incident.getFlowNodeId());
            }
            incDatas.put(incident.getId(), holder);
        }

    }

    private Map<String, String> getFlowNodeIds(Set<String> flowNodeInstanceIds) {
        HashMap<String, String> flowNodeIdsMap = new HashMap<String, String>();
        ;
        Query query = new Query.Builder()
                .terms(new TermsQuery.Builder()
                        .field(FlowNodeInstanceTemplate.ID)
                        .terms(terms -> terms.value(flowNodeInstanceIds.stream().map(FieldValue::of).collect(Collectors.toList())))
                        .build())
                .build();
        List<FlowNodeInstanceEntity> list = flowNodeInstanceDao.list(query, ElasticsearchUtil.QueryType.ONLY_RUNTIME);

        // 转换为 flowNodeInstanceId - flowNodeId 的 map
        return list.stream().collect(Collectors.toMap(FlowNodeInstanceEntity::getId, FlowNodeInstanceEntity::getFlowNodeId));
    }

    public FlowNodeInstanceDetailDto detailByFlowNodeId(String processInstanceId, String flowNodeId) {
        return null;
    }

}
