package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.dto.GroupDeployDto;
import com.brandnewdata.mop.poc.process.cache.DeployNoExpCache;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupDeployService {

    @Autowired
    private DeployNoExpCache cache;

    public Page<GroupDeployDto> groupDeployPage(int pageNum, int pageSize) {
        // 通过 cache 获取 group deploy map
        Map<String, GroupDeployDto> groupDeployMap = getAllGroupDeployMap();

        // 过滤得到分页
        Page<GroupDeployDto> page = filter(groupDeployMap, pageNum, pageSize);

        return page;
    }

    private Map<String, GroupDeployDto> getAllGroupDeployMap() {
        Map<String, GroupDeployDto> ret = new HashMap<>();
        Map<Long, ProcessDeployDto> processDeployMap = cache.asMap();
        if(CollUtil.isEmpty(processDeployMap)) {
            return ret;
        }

        for (ProcessDeployDto processDeployDTO : processDeployMap.values()) {
            String processId = processDeployDTO.getProcessId();
            String processName = processDeployDTO.getProcessName();
            int version = processDeployDTO.getVersion();

            LocalDateTime updateTime = processDeployDTO.getUpdateTime();
            // updateTime 不为空，就取 updateTime，否则就取 createTime
            updateTime = updateTime != null ? updateTime : processDeployDTO.getCreateTime();

            // 根据 processId 获取 group deploy
            GroupDeployDto groupDeployDTO = ret.get(processId);
            if(groupDeployDTO == null) {
                groupDeployDTO = new GroupDeployDto();
                groupDeployDTO.setProcessId(processId);
                groupDeployDTO.setProcessName(processName);
                groupDeployDTO.setLatestVersion(version);
                groupDeployDTO.setLatestUpdateTime(updateTime);
                List<ProcessDeployDto> list = new ArrayList<>();
                list.add(processDeployDTO);
                groupDeployDTO.setDeploys(list);
                ret.put(processId, groupDeployDTO);
            } else {
                // 追赠一个groupDeploy
                groupDeployDTO.getDeploys().add(processDeployDTO);
                if(version > groupDeployDTO.getLatestVersion()) {
                    // 更新最后版本,名称,时间
                    groupDeployDTO.setProcessName(processName);
                    groupDeployDTO.setLatestVersion(version);
                    groupDeployDTO.setLatestUpdateTime(updateTime);
                }
            }
        }



        return ret;
    }

    private Page<GroupDeployDto> filter(Map<String, GroupDeployDto> groupDeployMap, int pageNum, int pageSize) {
        Page<GroupDeployDto> ret = new Page<>();

        // 对所有分组完成的流程进行排序
        List<GroupDeployDto> values = CollUtil.sort(groupDeployMap.values(), (o1, o2) -> {
            LocalDateTime time1 = o1.getLatestUpdateTime();
            LocalDateTime time2 = o2.getLatestUpdateTime();
            return time2.compareTo(time1);
        });

        PageEnhancedUtil.setFirstPageNo(1);
        List<GroupDeployDto> filterList = PageEnhancedUtil.slice(pageNum, pageSize, values);

        for (GroupDeployDto groupDeployDTO : filterList) {
            List<ProcessDeployDto> deploys = groupDeployDTO.getDeploys();

            // 对 deploy 列表进行排序
            List<ProcessDeployDto> deployDTOS = deploys.stream().sorted((o1, o2) -> Integer.compare(o2.getVersion(), o1.getVersion()))
                    .collect(Collectors.toList());
            groupDeployDTO.setDeploys(deployDTOS);
        }

        ret.setTotal(values.size());
        ret.setRecords(filterList);
        return ret;
    }




}
