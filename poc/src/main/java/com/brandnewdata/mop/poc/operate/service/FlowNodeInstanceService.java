package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceDto;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeInstanceRequest;
import com.brandnewdata.mop.poc.operate.dto.FlowNodeStateDto;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.schema.template.FlowNodeInstanceTemplate;
import com.brandnewdata.mop.poc.operate.util.ElasticsearchUtil;
import lombok.Getter;
import lombok.Setter;
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
    private FlowNodeInstanceTemplate template;

    @Autowired
    private ElasticsearchClient client;

    public List<FlowNodeInstanceDto> list(String processInstanceId) {
        Assert.notNull(processInstanceId);

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(template.getAlias())
                .query(new Query.Builder()
                        .term(t -> t.field(FlowNodeInstanceTemplate.PROCESS_INSTANCE_KEY).value(processInstanceId))
                        .build()
                ).build();

        List<FlowNodeInstanceEntity> flowNodeInstanceEntities = ElasticsearchUtil.scrollAll(client, searchRequest, FlowNodeInstanceEntity.class);

        List<FlowNodeInstanceDto> flowNodeInstanceDtos = flowNodeInstanceEntities.stream()
                .map(entity -> new FlowNodeInstanceDto().fromEntity(entity)).collect(Collectors.toList());

        // 构造 path to instance 的 map
        Map<String, FlowNodeInstanceDto> pathInstanceMap = flowNodeInstanceDtos.stream()
                .collect(Collectors.toMap(FlowNodeInstanceDto::getTreePath, Function.identity()));

        // 转换为 treeNode
        List<TreeNode<String>> treeNodes = flowNodeInstanceDtos.stream().map(this::toTreeNode).collect(Collectors.toList());

        // 根节点是 process instance id
        List<Tree<String>> children = TreeUtil.build(treeNodes, processInstanceId);

        FlowNodeInstanceTreeDto ret = buildFlowNodeInstanceTree(children, pathInstanceMap);

        return ret.getList();
    }

    private TreeNode<String> toTreeNode(FlowNodeInstanceDto flowNodeInstanceDto) {
        // 将 flow node instance 中的 path 最为 id
        String id = flowNodeInstanceDto.getTreePath();

        // 将类似 START_EVENT_1 作为name
        String name = flowNodeInstanceDto.getFlowNodeId();

        // 将 id 作为排序字段
        String weight = flowNodeInstanceDto.getId();

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

    private FlowNodeInstanceTreeDto buildFlowNodeInstanceTree(List<Tree<String>> children, Map<String, FlowNodeInstanceDto> pathInstanceMap) {
        FlowNodeInstanceTreeDto ret = new FlowNodeInstanceTreeDto();
        List<FlowNodeInstanceDto> list = new ArrayList<>();
        ret.setList(list);
        ret.setIncident(false);
        if(CollUtil.isEmpty(children)) {
            return ret;
        }

        for (Tree<String> tree : children) {
            FlowNodeInstanceDto flowNodeInstanceDto = pathInstanceMap.get(tree.getId());
            if(flowNodeInstanceDto == null) throw new RuntimeException("path 数据错误");
            // 递归处理
            FlowNodeInstanceTreeDto childrenDto = buildFlowNodeInstanceTree(tree.getChildren(), pathInstanceMap);

            // if()
            flowNodeInstanceDto.setChildren(childrenDto.getList());

            if(childrenDto.isIncident() && !flowNodeInstanceDto.isIncident()) {
                // 子节点有错误事件，但是本节点无错误事件，修改本节点的事件和状态
                flowNodeInstanceDto.setIncident(true);
                flowNodeInstanceDto.setState(FlowNodeStateDto.INCIDENT);
            }

            if(flowNodeInstanceDto.isIncident() && !ret.isIncident()) {
                // 本节点有错误事件，但是总体结果无错误事件时，修改总体结果
                ret.setIncident(true);
            }
            list.add(flowNodeInstanceDto);
        }

        return ret;
    }




    @Getter
    @Setter
    private static class FlowNodeInstanceTreeDto {
        private boolean incident;
        private List<FlowNodeInstanceDto> list;
    }

}
