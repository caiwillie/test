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
import com.brandnewdata.mop.poc.operate.bo.IncidentBo;
import com.brandnewdata.mop.poc.operate.dao.EventDao;
import com.brandnewdata.mop.poc.operate.dao.FlowNodeInstanceDao;
import com.brandnewdata.mop.poc.operate.dao.IncidentDao;
import com.brandnewdata.mop.poc.operate.dao.ListViewDao;
import com.brandnewdata.mop.poc.operate.dto.*;
import com.brandnewdata.mop.poc.operate.manager.DaoManager;
import com.brandnewdata.mop.poc.operate.po.EventPo;
import com.brandnewdata.mop.poc.operate.po.FlowNodeInstancePo;
import com.brandnewdata.mop.poc.operate.po.FlowNodeType;
import com.brandnewdata.mop.poc.operate.po.IncidentPo;
import com.brandnewdata.mop.poc.operate.po.listview.ProcessInstanceForListViewPo;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.schema.template.ListViewTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import com.brandnewdata.mop.poc.operate.util.TreePathUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FlowNodeInstanceService implements IFlowNodeInstanceService {

    private final DaoManager daoManager;

    public FlowNodeInstanceService(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    @Override
    public List<FlowNodeInstanceTreeNodeDto> list(Long envId, String processInstanceId) {
        Assert.notNull(processInstanceId, "流程实例id不能为空");

        FlowNodeInstanceDao flowNodeInstanceDao = daoManager.getFlowNodeInstanceDaoByEnvId(envId);

        List<FlowNodeInstancePo> flowNodeInstanceEntities = flowNodeInstanceDao.list(processInstanceId);

        List<FlowNodeInstanceTreeNodeDto> flowNodeInstanceTreeNodeDTOS = flowNodeInstanceEntities.stream()
                .map(entity -> new FlowNodeInstanceTreeNodeDto().from(entity)).collect(Collectors.toList());

        // 构造 path to instance 的 map
        Map<String, FlowNodeInstanceTreeNodeDto> pathInstanceMap = flowNodeInstanceTreeNodeDTOS.stream()
                .collect(Collectors.toMap(FlowNodeInstanceTreeNodeDto::getTreePath, Function.identity()));

        // 转换为 treeNode
        List<TreeNode<String>> treeNodes = flowNodeInstanceTreeNodeDTOS.stream().map(this::toTreeNode).collect(Collectors.toList());

        // 根节点是 process instance id
        List<Tree<String>> children = TreeUtil.build(treeNodes, processInstanceId);

        FlowNodeInstanceTreeDto ret = buildFlowNodeInstanceTree(children, pathInstanceMap);

        return ret.getList();
    }

    @Override
    public FlowNodeInstanceDto detailById(Long envId, String flowNodeInstanceId) {
        Assert.notNull(flowNodeInstanceId, "节点实例id不能为空");

        FlowNodeInstanceDao flowNodeInstanceDao = daoManager.getFlowNodeInstanceDaoByEnvId(envId);

        // 首先查找 instance entity
        FlowNodeInstancePo flowNodeInstance = flowNodeInstanceDao.searchOne(flowNodeInstanceId);

        return getFlowNodeInstanceDetailDto(envId, flowNodeInstance);
    }

    @Override
    public FlowNodeInstanceDto detailByFlowNodeId(Long envId, Long processInstanceId, String flowNodeId) {
        return null;
    }

    private TreeNode<String> toTreeNode(FlowNodeInstanceTreeNodeDto flowNodeInstanceTreeNodeDto) {
        // 将 flow node instance 中的 path 最为 id
        String id = flowNodeInstanceTreeNodeDto.getTreePath();

        // 将类似 START_EVENT_1 作为name
        String name = flowNodeInstanceTreeNodeDto.getFlowNodeId();

        // 将 id 作为排序字段
        String weight = flowNodeInstanceTreeNodeDto.getId();

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

    private FlowNodeInstanceTreeDto buildFlowNodeInstanceTree(List<Tree<String>> children, Map<String, FlowNodeInstanceTreeNodeDto> pathInstanceMap) {
        FlowNodeInstanceTreeDto ret = new FlowNodeInstanceTreeDto();
        List<FlowNodeInstanceTreeNodeDto> list = new ArrayList<>();
        ret.setList(list);
        ret.setIncident(false);
        if (CollUtil.isEmpty(children)) {
            return ret;
        }

        for (Tree<String> tree : children) {
            FlowNodeInstanceTreeNodeDto flowNodeInstanceTreeNodeDto = pathInstanceMap.get(tree.getId());
            if (flowNodeInstanceTreeNodeDto == null) throw new RuntimeException("path 数据错误");
            // 递归处理
            FlowNodeInstanceTreeDto childrenDto = buildFlowNodeInstanceTree(tree.getChildren(), pathInstanceMap);

            // if()
            flowNodeInstanceTreeNodeDto.setChildren(childrenDto.getList());

            if (childrenDto.isIncident() || flowNodeInstanceTreeNodeDto.isIncident()) {
                // 子节点有错误事件，或者节点本生有错误
                ret.setIncident(true);
                flowNodeInstanceTreeNodeDto.setIncident(true);
                flowNodeInstanceTreeNodeDto.setState(FlowNodeStateDto.INCIDENT);
            }

            list.add(flowNodeInstanceTreeNodeDto);
        }

        return ret;
    }

    private FlowNodeInstanceDto getFlowNodeInstanceDetailDto(Long envId, FlowNodeInstancePo flowNodeInstancePo) {
        if(flowNodeInstancePo == null) return null;

        FlowNodeInstanceDto ret = new FlowNodeInstanceDto();

        String flowNodeInstanceId = flowNodeInstancePo.getId();

        EventDao eventDao = daoManager.getEventDaoByEnvId(envId);

        // 查找 event
        EventPo eventEntity = eventDao.getOne(flowNodeInstanceId);

        // 转换 metaData
        FlowNodeInstanceMetaDataDto metaData =
                new FlowNodeInstanceMetaDataDto().fromEntity(flowNodeInstancePo, eventEntity);

        // 添加 call activity
        addCallActivityMetaData(envId, metaData);

        // 查找 incident
        IncidentBo incidentBo = searchIncidentByFlowNodeInstanceId(envId,
                flowNodeInstancePo.getProcessInstanceKey(), flowNodeInstancePo.getFlowNodeId(),
                flowNodeInstancePo.getId(), flowNodeInstancePo.getType());
        Integer incidentCount = incidentBo.getCount();
        IncidentDto incidentDto = incidentBo.getIncidentDTO();


        ret.setRepeated(false);
        ret.setMetaData(metaData);
        ret.setIncidentCount(incidentCount);
        ret.setIncident(incidentDto);
        return ret;
    }

    /**
     * 如果是call activity，就查询更多内容
     */
    private void addCallActivityMetaData(Long envId, FlowNodeInstanceMetaDataDto metaData) {
        if (metaData.getFlowNodeType().equals(FlowNodeType.CALL_ACTIVITY)) {
            ListViewDao listViewDao = daoManager.getListViewDaoByEnvId(envId);
            ProcessInstanceForListViewPo entity = listViewDao.getOneByParentFlowNodeInstanceId(metaData.getFlowNodeInstanceId());
            String calledProcessInstanceId = entity.getId();
            String processName = entity.getProcessName();
            if (processName == null) {
                processName = entity.getBpmnProcessId();
            }
            metaData.setCalledProcessInstanceId(calledProcessInstanceId);
            metaData.setCalledProcessDefinitionName(processName);
        }
    }

    private IncidentBo searchIncidentByFlowNodeInstanceId(Long envId, Long processInstanceId, String flowNodeId,
                                                          String flowNodeInstanceId, FlowNodeType flowNodeType) {
        // 类似 PI_{processInstanceId}
        String processInstancePath = getProcessInstanceTreePath(envId, String.valueOf(processInstanceId));

        TreePathUtil treePath = new TreePathUtil(processInstancePath);
        treePath.appendFlowNode(flowNodeId);
        treePath.appendFlowNodeInstance(flowNodeInstanceId);
        String searchPath = treePath.toString();

        return searchIncident(envId, processInstanceId, processInstancePath, searchPath, flowNodeType);
    }

    private String getProcessInstanceTreePath(Long envId, String processInstanceId) {
        Query query = new Query.Builder()
                .bool(new BoolQuery.Builder()
                        .must(new Query.Builder()
                                .term(t -> t.field(ListViewTemplate.JOIN_RELATION).value("processInstance"))
                                .build(), new Query.Builder()

                                .term(t -> t.field(ListViewTemplate.KEY).value(processInstanceId))
                                .build())
                        .build())
                .build();

        ListViewDao listViewDao = daoManager.getListViewDaoByEnvId(envId);
        ProcessInstanceForListViewPo entity = listViewDao.searchOne(query);
        String treePath = Optional.ofNullable(entity).map(ProcessInstanceForListViewPo::getTreePath).orElse(null);
        Assert.notNull(treePath);

        return treePath;
    }


    private IncidentBo searchIncident(Long envId, Long processInstanceId,
                                      String processInstancePath, String searchPath, FlowNodeType flowNodeType) {
        IncidentBo ret = new IncidentBo();
        IncidentDto incidentDto = new IncidentDto();

        IncidentDao incidentDao = daoManager.getIncidentDaoByEnvId(envId);

        List<IncidentPo> incidentEntities = incidentDao.listByTreePath(searchPath);

        // 获取incident的个数
        int incidentCount = incidentEntities.size();
        ret.setCount(incidentCount);

        if (incidentCount != 1) {
            // 如果事件个数不是 1， 就直接返回
            return ret;
        }

        // 查询详情
        IncidentPo incidentPo = incidentEntities.get(0);

        incidentDto.fromEntity(incidentPo);

        // 父子调用 错误冒泡
        Map<String, IncidentDataHolder> incidentDataHolderMap =
                collectFlowNodeDataForPropagatedIncidents(envId, ListUtil.of(incidentPo), processInstanceId, processInstancePath);


        DecisionInstanceReferenceDto rootCauseDecision = null;
        if (flowNodeType.equals(FlowNodeType.BUSINESS_RULE_TASK)) {
            // todo 处理 business rule
        }

        // 这说明就是发生了call activity
        IncidentDataHolder dataHolder = incidentDataHolderMap.get(incidentPo.getId());

        if (dataHolder != null && !Objects.equals(incidentDto.getFlowNodeInstanceId(),
                dataHolder.getFinalFlowNodeInstanceId())) {
            // 如果data holder中的 flowNodeInstanceId 和 incidentDto中不一样，就替换成out_activity
            incidentDto.setFlowNodeId(dataHolder.getFinalFlowNodeId());
            incidentDto.setFlowNodeInstanceId(dataHolder.getFinalFlowNodeInstanceId());
            // 设置 inner_activity 的错误
            ProcessInstanceReferenceDto rootCauseInstance = new ProcessInstanceReferenceDto();
            rootCauseInstance.setInstanceId(String.valueOf(incidentPo.getProcessInstanceKey()));
            rootCauseInstance.setProcessDefinitionId(String.valueOf(incidentPo.getProcessDefinitionKey()));
            // todo
            // rootCauseInstance.setProcessDefinitionName(getProcessName(incidentEntity.getProcessDefinitionKey()));
            incidentDto.setRootCauseInstance(rootCauseInstance);
        }

        if (rootCauseDecision != null) incidentDto.setRootCauseDecision(rootCauseDecision);

        ret.setIncidentDTO(incidentDto);
        return ret;
    }

    private Map<String, IncidentDataHolder> collectFlowNodeDataForPropagatedIncidents(Long envId,
                                                                                      List<IncidentPo> incidents, Long processInstanceId, String currentTreePath) {

        HashSet<String> flowNodeInstanceIdsSet = new HashSet<>();
        HashMap<String, IncidentDataHolder> incDatas = new HashMap<>();
        Iterator<IncidentPo> iterator = incidents.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                // caiwillie 查询结束后，查出最终的 finalFlowNodeId
                if (flowNodeInstanceIdsSet.size() <= 0) return incDatas;
                Map<String, String> flowNodeIdsMap = this.getFlowNodeIds(envId, flowNodeInstanceIdsSet);
                incDatas.values().stream().filter(iData -> iData.getFinalFlowNodeId() == null)
                        .forEach(iData -> iData.setFinalFlowNodeId(flowNodeIdsMap.get(iData.getFinalFlowNodeInstanceId())));
                return incDatas;
            }
            // 首先获取下一个incident
            IncidentPo incident = iterator.next();
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

    private Map<String, String> getFlowNodeIds(Long envId, Set<String> flowNodeInstanceIds) {
        HashMap<String, String> flowNodeIdsMap = new HashMap<String, String>();

        Query query = new Query.Builder()
                .terms(new TermsQuery.Builder()
                        .field(FlowNodeInstanceTemplate.ID)
                        .terms(terms -> terms.value(flowNodeInstanceIds.stream().map(FieldValue::of).collect(Collectors.toList())))
                        .build())
                .build();

        FlowNodeInstanceDao flowNodeInstanceDao = daoManager.getFlowNodeInstanceDaoByEnvId(envId);
        List<FlowNodeInstancePo> list = flowNodeInstanceDao.list(query, ElasticsearchUtil.QueryType.ONLY_RUNTIME);

        // 转换为 flowNodeInstanceId - flowNodeId 的 map
        return list.stream().collect(Collectors.toMap(FlowNodeInstancePo::getId, FlowNodeInstancePo::getFlowNodeId));
    }



}
