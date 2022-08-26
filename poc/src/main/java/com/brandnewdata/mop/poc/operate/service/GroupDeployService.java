package com.brandnewdata.mop.poc.operate.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.PageUtil;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.operate.cache.DeployCache;
import com.brandnewdata.mop.poc.operate.dto.GroupDeployDTO;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GroupDeployService {

    @Autowired
    private DeployCache cache;

    public Page<GroupDeployDTO> groupDeployPage(int pageNum, int pageSize) {
        // 通过 cache 获取 group deploy map
        Map<String, GroupDeployDTO> groupDeployMap = getAllGroupDeployMap();

        // 过滤得到分页
        Page<GroupDeployDTO> page = filter(groupDeployMap, pageNum, pageSize);

        return page;
    }

    private Map<String, GroupDeployDTO> getAllGroupDeployMap() {
        Map<String, GroupDeployDTO> ret = new HashMap<>();
        Map<Long, ProcessDeployDTO> processDeployMap = cache.asMap();
        if(CollUtil.isEmpty(processDeployMap)) {
            return ret;
        }

        for (ProcessDeployDTO processDeployDTO : processDeployMap.values()) {
            String processId = processDeployDTO.getProcessId();
            String processName = processDeployDTO.getProcessName();
            int version = processDeployDTO.getVersion();
            LocalDateTime createTime = processDeployDTO.getCreateTime();

            GroupDeployDTO groupDeployDTO = ret.get(processId);
            if(groupDeployDTO == null) {
                groupDeployDTO = new GroupDeployDTO();
                groupDeployDTO.setProcessId(processId);
                groupDeployDTO.setProcessName(processName);
                groupDeployDTO.setLatestVersion(version);
                groupDeployDTO.setLatestCreateTime(createTime);
                List<ProcessDeployDTO> list = new ArrayList<>();
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
                    groupDeployDTO.setLatestCreateTime(createTime);
                }
            }
        }

        return ret;
    }

    private Page<GroupDeployDTO> filter(Map<String, GroupDeployDTO> groupDeployMap, int pageNum, int pageSize) {
        Page<GroupDeployDTO> ret = new Page<>();

        Collection<GroupDeployDTO> values = groupDeployMap.values();
        List<GroupDeployDTO> newValues = CollUtil.sort(values, (o1, o2) -> {
            LocalDateTime time1 = o1.getLatestCreateTime();
            LocalDateTime time2 = o2.getLatestCreateTime();
            return time1.compareTo(time2);
        });

        List<GroupDeployDTO> filterList = new ArrayList<>();
        PageUtil.setFirstPageNo(1);
        int[] startEnd = PageUtil.transToStartEnd(pageNum, pageSize);
        int start = startEnd[0];
        int end = startEnd[1];
        int total = newValues.size();
        for (int i = start; i < total && i < end; i++) {
            filterList.add(newValues.get(i));
        }

        ret.setTotal(total);
        ret.setRecords(filterList);
        return ret;
    }


}
