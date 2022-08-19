package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import com.brandnewdata.mop.poc.operate.dao.EventDao;
import com.brandnewdata.mop.poc.operate.dao.FlowNodeInstanceDao;
import com.brandnewdata.mop.poc.operate.dto.*;
import com.brandnewdata.mop.poc.operate.entity.EventEntity;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author caiwillie
 */
@Service
public class FlowNodeInstanceService {

    @Autowired
    private FlowNodeInstanceDao flowNodeInstanceDao;

    @Autowired
    private EventDao eventDao;

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
        if(index > 0) {
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
        if(CollUtil.isEmpty(children)) {
            return ret;
        }

        for (Tree<String> tree : children) {
            FlowNodeInstanceListDto flowNodeInstanceListDto = pathInstanceMap.get(tree.getId());
            if(flowNodeInstanceListDto == null) throw new RuntimeException("path 数据错误");
            // 递归处理
            FlowNodeInstanceTreeDto childrenDto = buildFlowNodeInstanceTree(tree.getChildren(), pathInstanceMap);

            // if()
            flowNodeInstanceListDto.setChildren(childrenDto.getList());

            if(childrenDto.isIncident() && !flowNodeInstanceListDto.isIncident()) {
                // 子节点有错误事件，但是本节点无错误事件，修改本节点的事件和状态
                flowNodeInstanceListDto.setIncident(true);
                flowNodeInstanceListDto.setState(FlowNodeStateDto.INCIDENT);
            }

            if(flowNodeInstanceListDto.isIncident() && !ret.isIncident()) {
                // 本节点有错误事件，但是总体结果无错误事件时，修改总体结果
                ret.setIncident(true);
            }
            list.add(flowNodeInstanceListDto);
        }

        return ret;
    }

    public FlowNodeInstanceDetailDto detailByFlowNodeInstanceId(String processInstanceId, String flowNodeInstanceId) {
        Assert.notNull(flowNodeInstanceId);

        // 首先查找 instance entity
        FlowNodeInstanceEntity flowNodeInstance = flowNodeInstanceDao.getOne(flowNodeInstanceId);

        // 查找 event
        EventEntity eventEntity = eventDao.getOne(flowNodeInstanceId);

        // 转换 metaData
        FlowNodeInstanceMetaDataDto flowNodeInstanceMetaDataDto =
                new FlowNodeInstanceMetaDataDto().fromEntity(flowNodeInstance, eventEntity);

        // 查找 incident

        return null;
    }


    public FlowNodeInstanceDetailDto detailByFlowNodeId(String processInstanceId, String flowNodeId) {
        return null;
    };




}
