package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.NumberUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import com.brandnewdata.mop.poc.operate.cache.ProcessCache;
import com.brandnewdata.mop.poc.operate.dao.EventDao;
import com.brandnewdata.mop.poc.operate.dao.FlowNodeInstanceDao;
import com.brandnewdata.mop.poc.operate.dao.IncidentDao;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dto.*;
import com.brandnewdata.mop.poc.operate.entity.*;
import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.brandnewdata.mop.poc.operate.util.TreePathUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brandnewdata.mop.poc.operate.dto.IncidentDTO.FALLBACK_PROCESS_DEFINITION_NAME;

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

    @Autowired
    private ProcessCache processCache;

    public List<FlowNodeInstanceDTO> list(String processInstanceId) {
        Assert.notNull(processInstanceId);

        List<FlowNodeInstanceEntity> flowNodeInstanceEntities = flowNodeInstanceDao.list(processInstanceId);

        List<FlowNodeInstanceDTO> flowNodeInstanceDTOS = flowNodeInstanceEntities.stream()
                .map(entity -> new FlowNodeInstanceDTO().from(entity)).collect(Collectors.toList());

        // 构造 path to instance 的 map
        Map<String, FlowNodeInstanceDTO> pathInstanceMap = flowNodeInstanceDTOS.stream()
                .collect(Collectors.toMap(FlowNodeInstanceDTO::getTreePath, Function.identity()));

        // 转换为 treeNode
        List<TreeNode<String>> treeNodes = flowNodeInstanceDTOS.stream().map(this::toTreeNode).collect(Collectors.toList());

        // 根节点是 process instance id
        List<Tree<String>> children = TreeUtil.build(treeNodes, processInstanceId);

        FlowNodeInstanceTreeDTO ret = buildFlowNodeInstanceTree(children, pathInstanceMap);

        return ret.getList();
    }

    private TreeNode<String> toTreeNode(FlowNodeInstanceDTO flowNodeInstanceDto) {
        // 将 flow node instance 中的 path 最为 id
        String id = flowNodeInstanceDto.getTreePath();

        // 将类似 START_EVENT_1 作为name
        String name = flowNodeInstanceDto.getFlowNodeId();

        // 将 id 作为排序字段
        String weight = flowNodeInstanceDto.getId();

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

    private FlowNodeInstanceTreeDTO buildFlowNodeInstanceTree(List<Tree<String>> children, Map<String, FlowNodeInstanceDTO> pathInstanceMap) {
        FlowNodeInstanceTreeDTO ret = new FlowNodeInstanceTreeDTO();
        List<FlowNodeInstanceDTO> list = new ArrayList<>();
        ret.setList(list);
        ret.setIncident(false);
        if (CollUtil.isEmpty(children)) {
            return ret;
        }

        for (Tree<String> tree : children) {
            FlowNodeInstanceDTO flowNodeInstanceDto = pathInstanceMap.get(tree.getId());
            if (flowNodeInstanceDto == null) throw new RuntimeException("path 数据错误");
            // 递归处理
            FlowNodeInstanceTreeDTO childrenDto = buildFlowNodeInstanceTree(tree.getChildren(), pathInstanceMap);

            // if()
            flowNodeInstanceDto.setChildren(childrenDto.getList());

            if (childrenDto.isIncident() || flowNodeInstanceDto.isIncident()) {
                // 子节点有错误事件，或者节点本生有错误
                ret.setIncident(true);
                flowNodeInstanceDto.setIncident(true);
                flowNodeInstanceDto.setState(FlowNodeStateDTO.INCIDENT);
            }

            list.add(flowNodeInstanceDto);
        }

        return ret;
    }

    public FlowNodeInstanceDetailDTO detailByFlowNodeInstanceId(String processInstanceId, String flowNodeInstanceId) {
        FlowNodeInstanceDetailDTO ret = new FlowNodeInstanceDetailDTO();

        Assert.notNull(flowNodeInstanceId);

        // 首先查找 instance entity
        FlowNodeInstanceEntity flowNodeInstance = flowNodeInstanceDao.searchOne(flowNodeInstanceId);

        return getFlowNodeInstanceDetailDTO(flowNodeInstance);
    }


    public FlowNodeInstanceDetailDTO detailByFlowNodeId(Long processInstanceId, String flowNodeId) {

        FlowNodeInstanceDetailDTO ret = new FlowNodeInstanceDetailDTO();

        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                                .build(), new Query.Builder()
                                .term(t -> t.field(FlowNodeInstanceTemplate.FLOW_NODE_ID).value(flowNodeId))
                                .build())
                        .build())
                .build();

        List<FlowNodeInstanceEntity> entities = flowNodeInstanceDao.list(query, ElasticsearchUtil.QueryType.ALL);

        if(CollUtil.isEmpty(entities)) {
            return ret;
        }

        FlowNodeInstanceEntity flowNodeInstanceEntity = entities.get(0);
        if(entities.size() == 1) {
            return getFlowNodeInstanceDetailDTO(entities.get(0));
        } else {
            FlowNodeType flowNodeType = flowNodeInstanceEntity.getType();
            ret.setInstanceCount(entities.size());
            ret.setFlowNodeId(flowNodeId);
            ret.setFlowNodeType(flowNodeType.name());

            IncidentInfo incidentInfo = searchIncidentByFlowNodeId(processInstanceId, flowNodeId, flowNodeType);
            ret.setIncidentCount(incidentInfo.getCount());
            ret.setIncident(incidentInfo.getIncidentDTO());
        }

        return ret;
    }

    private FlowNodeInstanceDetailDTO getFlowNodeInstanceDetailDTO(FlowNodeInstanceEntity flowNodeInstanceEntity) {
        if(flowNodeInstanceEntity == null) return null;

        FlowNodeInstanceDetailDTO ret = new FlowNodeInstanceDetailDTO();

        String flowNodeInstanceId = flowNodeInstanceEntity.getId();

        // 查找 event
        EventEntity eventEntity = eventDao.getOne(flowNodeInstanceId);

        // 转换 metaData
        FlowNodeInstanceMetaDataDTO metaData =
                new FlowNodeInstanceMetaDataDTO().fromEntity(flowNodeInstanceEntity, eventEntity);

        // 添加 call activity
        addCallActivityMetaData(metaData);

        // 查找 incident
        IncidentInfo incidentInfo = searchIncidentByFlowNodeInstanceId(
                flowNodeInstanceEntity.getProcessInstanceKey(), flowNodeInstanceEntity.getFlowNodeId(),
                flowNodeInstanceEntity.getId(), flowNodeInstanceEntity.getType());
        Integer incidentCount = incidentInfo.getCount();
        IncidentDTO incidentDto = incidentInfo.getIncidentDTO();


        ret.setRepeated(false);
        ret.setMetaData(metaData);
        ret.setIncidentCount(incidentCount);
        ret.setIncident(incidentDto);
        return ret;
    }

    /**
     * 如果是call activity，就查询更多内容
     */
    private void addCallActivityMetaData(FlowNodeInstanceMetaDataDTO metaData) {
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

    private void addBusinessRuleTaskMetaData(FlowNodeInstanceMetaDataDTO metaData) {
        if (metaData.getFlowNodeType().equals(FlowNodeType.BUSINESS_RULE_TASK)) {

        }
    }

    private IncidentInfo searchIncidentByFlowNodeInstanceId(Long processInstanceId, String flowNodeId,
                                                            String flowNodeInstanceId, FlowNodeType flowNodeType) {
        // 类似 PI_{processInstanceId}
        String processInstancePath = getProcessInstanceTreePath(String.valueOf(processInstanceId));

        TreePathUtil treePath = new TreePathUtil(processInstancePath);
        treePath.appendFlowNode(flowNodeId);
        treePath.appendFlowNodeInstance(flowNodeInstanceId);
        String searchPath = treePath.toString();

        return searchIncident(processInstanceId, processInstancePath, searchPath, flowNodeType);
    }

    private IncidentInfo searchIncidentByFlowNodeId(Long processInstanceId, String flowNodeId, FlowNodeType flowNodeType) {
        // 类似 PI_{processInstanceId}
        String processInstancePath = getProcessInstanceTreePath(String.valueOf(processInstanceId));

        TreePathUtil treePath = new TreePathUtil(processInstancePath);
        treePath.appendFlowNode(flowNodeId);
        String searchPath = treePath.toString();

        return searchIncident(processInstanceId, processInstancePath, searchPath, flowNodeType);
    }

    private IncidentInfo searchIncident(Long processInstanceId, String processInstancePath, String searchPath, FlowNodeType flowNodeType) {
        IncidentInfo ret = new IncidentInfo();
        IncidentDTO incidentDto = new IncidentDTO();

        List<IncidentEntity> incidentEntities = incidentDao.listByTreePath(searchPath);

        // 获取incident的个数
        int incidentCount = incidentEntities.size();
        ret.setCount(incidentCount);

        if (incidentCount != 1) {
            // 如果事件个数不是 1， 就直接返回
            return ret;
        }

        // 查询详情
        IncidentEntity incidentEntity = incidentEntities.get(0);

        incidentDto.fromEntity(incidentEntity);

        // 父子调用 错误冒泡
        Map<String, IncidentDataHolder> incidentDataHolderMap =
                collectFlowNodeDataForPropagatedIncidents(ListUtil.of(incidentEntity), processInstanceId, processInstancePath);


        DecisionInstanceReferenceDTO rootCauseDecision = null;
        if (flowNodeType.equals(FlowNodeType.BUSINESS_RULE_TASK)) {
            // todo 处理 business rule
        }

        // 这说明就是发生了call activity
        IncidentDataHolder dataHolder = incidentDataHolderMap.get(incidentEntity.getId());

        if (dataHolder != null && !Objects.equals(incidentDto.getFlowNodeInstanceId(),
                dataHolder.getFinalFlowNodeInstanceId())) {
            // 如果data holder中的 flowNodeInstanceId 和 incidentDto中不一样，就替换成out_activity
            incidentDto.setFlowNodeId(dataHolder.getFinalFlowNodeId());
            incidentDto.setFlowNodeInstanceId(dataHolder.getFinalFlowNodeInstanceId());
            // 设置 inner_activity 的错误
            ProcessInstanceReferenceDTO rootCauseInstance = new ProcessInstanceReferenceDTO();
            rootCauseInstance.setInstanceId(String.valueOf(incidentEntity.getProcessInstanceKey()));
            rootCauseInstance.setProcessDefinitionId(String.valueOf(incidentEntity.getProcessDefinitionKey()));
            rootCauseInstance.setProcessDefinitionName(getProcessName(incidentEntity.getProcessDefinitionKey()));
            incidentDto.setRootCauseInstance(rootCauseInstance);
        }

        if (rootCauseDecision != null) incidentDto.setRootCauseDecision(rootCauseDecision);

        ret.setIncidentDTO(incidentDto);
        return ret;
    }

    private String getProcessName(Long processDefinitionKey) {
        ProcessEntity processEntity = processCache.getOne(processDefinitionKey);
        if(processEntity != null) {
            return processEntity.getName() != null ? processEntity.getName() : processEntity.getBpmnProcessId();
        } else {
            return FALLBACK_PROCESS_DEFINITION_NAME;
        }
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
            // 首先获取下一个incident
            IncidentEntity incident = iterator.next();
            IncidentDataHolder holder = new IncidentDataHolder();
            holder.setIncidentId(incident.getId());
            if (!NumberUtil.equals(incident.getProcessInstanceKey(), processInstanceId)) {
                // 如果不相等，说明 call activity 发生异常
                // 这里是为了提取 out_activity 的 flowNodeInstance id
                /**
                 *{
                 * 		"id": "2251799813692198",
                 * 		"key": 2251799813692198,
                 * 		"partitionId": 1,
                 * 		"errorType": "IO_MAPPING_ERROR",
                 * 		"errorMessage": "failed to evaluate expression '{name: name}': no variable found for name 'name'",
                 * 		"errorMessageHash": 405587117,
                 * 		"state": "ACTIVE",
                 * 		"flowNodeId": "Activity_176r4ge",
                 * 		"flowNodeInstanceKey": 2251799813692197,
                 * 		"jobKey": null,
                 * 		"processInstanceKey": 2251799813692194,
                 * 		"creationTime": "2022-08-23T07:01:14.515+0000",
                 * 		"processDefinitionKey": 2251799813692170,
                 * 		"treePath": "PI_2251799813692189/FN_Activity_1hzdizx/FNI_2251799813692193/PI_2251799813692194/FN_Activity_176r4ge/FNI_2251799813692197",
                 * 		"pending": false
                 *        }
                 *
                 *
                 * 通过这个数据可以很明显看到，如果发生父子流程调用而导致的异常，incident中记录的是子流程的事件
                 */
                String callActivityInstanceId = TreePathUtil.extractFlowNodeInstanceId(incident.getTreePath(), currentTreePath);
                holder.setFinalFlowNodeInstanceId(callActivityInstanceId);
                flowNodeInstanceIdsSet.add(callActivityInstanceId);
                incDatas.put(incident.getId(), holder);
            }
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

    private String getProcessInstanceTreePath(String processInstanceId) {
        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build(), new Query.Builder()

                                .term(t -> t.field(ListViewTemplate.KEY).value(processInstanceId))
                                .build())
                        .build())
                .build();
        ProcessInstanceForListViewEntity entity = listViewDao.searchOne(query);
        String treePath = Optional.ofNullable(entity).map(ProcessInstanceForListViewEntity::getTreePath).orElse(null);
        Assert.notNull(treePath);

        return treePath;
    }

    @Getter
    @Setter
    private static class IncidentInfo {
        private int count;
        private IncidentDTO incidentDTO;
    }


}
