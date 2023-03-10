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
        Assert.notNull(processInstanceId, "????????????id????????????");

        FlowNodeInstanceDao flowNodeInstanceDao = daoManager.getFlowNodeInstanceDaoByEnvId(envId);

        List<FlowNodeInstancePo> flowNodeInstanceEntities = flowNodeInstanceDao.list(processInstanceId);

        List<FlowNodeInstanceTreeNodeDto> flowNodeInstanceTreeNodeDTOS = flowNodeInstanceEntities.stream()
                .map(entity -> new FlowNodeInstanceTreeNodeDto().from(entity)).collect(Collectors.toList());

        // ?????? path to instance ??? map
        Map<String, FlowNodeInstanceTreeNodeDto> pathInstanceMap = flowNodeInstanceTreeNodeDTOS.stream()
                .collect(Collectors.toMap(FlowNodeInstanceTreeNodeDto::getTreePath, Function.identity()));

        // ????????? treeNode
        List<TreeNode<String>> treeNodes = flowNodeInstanceTreeNodeDTOS.stream().map(this::toTreeNode).collect(Collectors.toList());

        // ???????????? process instance id
        List<Tree<String>> children = TreeUtil.build(treeNodes, processInstanceId);

        FlowNodeInstanceTreeDto ret = buildFlowNodeInstanceTree(children, pathInstanceMap);

        return ret.getList();
    }

    @Override
    public FlowNodeInstanceDto detailById(Long envId, String flowNodeInstanceId) {
        Assert.notNull(flowNodeInstanceId, "????????????id????????????");

        FlowNodeInstanceDao flowNodeInstanceDao = daoManager.getFlowNodeInstanceDaoByEnvId(envId);

        // ???????????? instance entity
        FlowNodeInstancePo flowNodeInstance = flowNodeInstanceDao.searchOne(flowNodeInstanceId);

        return getFlowNodeInstanceDetailDto(envId, flowNodeInstance);
    }

    @Override
    public FlowNodeInstanceDto detailByFlowNodeId(Long envId, Long processInstanceId, String flowNodeId) {
        return null;
    }

    private TreeNode<String> toTreeNode(FlowNodeInstanceTreeNodeDto flowNodeInstanceTreeNodeDto) {
        // ??? flow node instance ?????? path ?????? id
        String id = flowNodeInstanceTreeNodeDto.getTreePath();

        // ????????? START_EVENT_1 ??????name
        String name = flowNodeInstanceTreeNodeDto.getFlowNodeId();

        // ??? id ??????????????????
        String weight = flowNodeInstanceTreeNodeDto.getId();

        // ?????? id ??????????????? /
        int index = id.lastIndexOf('/');
        String parent = null;
        if (index > 0) {
            parent = id.substring(0, index);
        } else {
            throw new RuntimeException("tree path ????????????");
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
            if (flowNodeInstanceTreeNodeDto == null) throw new RuntimeException("path ????????????");
            // ????????????
            FlowNodeInstanceTreeDto childrenDto = buildFlowNodeInstanceTree(tree.getChildren(), pathInstanceMap);

            // if()
            flowNodeInstanceTreeNodeDto.setChildren(childrenDto.getList());

            if (childrenDto.isIncident() || flowNodeInstanceTreeNodeDto.isIncident()) {
                // ??????????????????????????????????????????????????????
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

        // ?????? event
        EventPo eventEntity = eventDao.getOne(flowNodeInstanceId);

        // ?????? metaData
        FlowNodeInstanceMetaDataDto metaData =
                new FlowNodeInstanceMetaDataDto().fromEntity(flowNodeInstancePo, eventEntity);

        // ?????? call activity
        addCallActivityMetaData(envId, metaData);

        // ?????? incident
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
     * ?????????call activity????????????????????????
     */
    private void addCallActivityMetaData(Long envId, FlowNodeInstanceMetaDataDto metaData) {
        if (metaData.getFlowNodeType().equals(FlowNodeType.CALL_ACTIVITY)) {
            ListViewDao listViewDao = daoManager.getListViewDaoByEnvId(envId);
            ProcessInstanceForListViewPo entity = listViewDao.getOneByParentFlowNodeInstanceId(metaData.getFlowNodeInstanceId());
            // ???????????????parent??????????????????
            if(entity == null) return;
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
        // ?????? PI_{processInstanceId}
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

        // ??????incident?????????
        int incidentCount = incidentEntities.size();
        ret.setCount(incidentCount);

        if (incidentCount != 1) {
            // ???????????????????????? 1??? ???????????????
            return ret;
        }

        // ????????????
        IncidentPo incidentPo = incidentEntities.get(0);

        incidentDto.fromEntity(incidentPo);

        // ???????????? ????????????
        Map<String, IncidentDataHolder> incidentDataHolderMap =
                collectFlowNodeDataForPropagatedIncidents(envId, ListUtil.of(incidentPo), processInstanceId, processInstancePath);


        DecisionInstanceReferenceDto rootCauseDecision = null;
        if (flowNodeType.equals(FlowNodeType.BUSINESS_RULE_TASK)) {
            // todo ?????? business rule
        }

        // ????????????????????????call activity
        IncidentDataHolder dataHolder = incidentDataHolderMap.get(incidentPo.getId());

        if (dataHolder != null && !Objects.equals(incidentDto.getFlowNodeInstanceId(),
                dataHolder.getFinalFlowNodeInstanceId())) {
            // ??????data holder?????? flowNodeInstanceId ??? incidentDto???????????????????????????out_activity
            incidentDto.setFlowNodeId(dataHolder.getFinalFlowNodeId());
            incidentDto.setFlowNodeInstanceId(dataHolder.getFinalFlowNodeInstanceId());
            // ?????? inner_activity ?????????
            ProcessInstanceReferenceDto rootCauseInstance = new ProcessInstanceReferenceDto();
            rootCauseInstance.setInstanceId(String.valueOf(incidentPo.getProcessInstanceKey()));
            rootCauseInstance.setProcessDefinitionId(String.valueOf(incidentPo.getProcessDefinitionKey()));
            // todo
            // rootCauseInstance.setProcessDefinitionName(getProcessName(incidentEntity.getProcessDefinitionKey()));
            incidentDto.setRootCauseInstance(rootCauseInstance);
        }

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
                // caiwillie ????????????????????????????????? finalFlowNodeId
                if (flowNodeInstanceIdsSet.size() <= 0) return incDatas;
                Map<String, String> flowNodeIdsMap = this.getFlowNodeIds(envId, flowNodeInstanceIdsSet);
                incDatas.values().stream().filter(iData -> iData.getFinalFlowNodeId() == null)
                        .forEach(iData -> iData.setFinalFlowNodeId(flowNodeIdsMap.get(iData.getFinalFlowNodeInstanceId())));
                return incDatas;
            }
            // ?????????????????????incident
            IncidentPo incident = iterator.next();
            IncidentDataHolder holder = new IncidentDataHolder();
            holder.setIncidentId(incident.getId());
            if (!NumberUtil.equals(incident.getProcessInstanceKey(), processInstanceId)) {
                // ???????????????????????? call activity ????????????
                // ????????????????????? out_activity ??? flowNodeInstance id
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
                 * ?????????????????????????????????????????????????????????????????????????????????????????????incident?????????????????????????????????
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

        // ????????? flowNodeInstanceId - flowNodeId ??? map
        return list.stream().collect(Collectors.toMap(FlowNodeInstancePo::getId, FlowNodeInstancePo::getFlowNodeId));
    }



}
