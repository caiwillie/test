package com.brandnewdata.mop.poc.operate.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDeployDto;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployEntity;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DeployCache {

    @Resource
    private ProcessDeployDao processDeployDao;

    private long lastId = 0;

    private Cache<Long, ProcessDeployDto> cache = CacheBuilder.newBuilder().build();

    @Scheduled(fixedDelay = 1000)
    public void load() {
        long lastId = this.lastId;
        List<ProcessDeployDto> tempList = new ArrayList<>();

        do {
            QueryWrapper<ProcessDeployEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.gt(ProcessDeployEntity.ID, lastId);
            queryWrapper.last("limit 40");

            List<ProcessDeployEntity> entities = processDeployDao.selectList(queryWrapper);

            // 清空临时数组
            tempList.clear();
            if(CollUtil.isNotEmpty(entities)) {
                for (ProcessDeployEntity entity : entities) {
                    ProcessDeployDto processDeployDTO = new ProcessDeployDto();
                    processDeployDTO.setId(entity.getId());
                    processDeployDTO.setProcessId(entity.getProcessId());
                    processDeployDTO.setProcessName(entity.getProcessName());
                    processDeployDTO.setVersion(entity.getVersion());
                    processDeployDTO.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
                    processDeployDTO.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
                    processDeployDTO.setZeebeKey(entity.getZeebeKey());
                    tempList.add(processDeployDTO);
                }

                ProcessDeployEntity lastProcessDeploy = entities.get(entities.size() - 1);
                lastId = lastProcessDeploy.getId();

                for (ProcessDeployDto processDeployDTO : tempList) {
                    cache.put(processDeployDTO.getId(), processDeployDTO);
                }
            }
        } while(CollUtil.isNotEmpty(tempList));

        // 更新最后的下标
        this.lastId = lastId;
    }

    public Map<Long, ProcessDeployDto> asMap() {
        return cache.asMap();
    }

}
