package com.brandnewdata.mop.poc.operate.cache;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.operate.entity.ProcessEntity;
import com.brandnewdata.mop.poc.process.dao.ProcessDeployDao;
import com.brandnewdata.mop.poc.process.dto.ProcessDeploy;
import com.brandnewdata.mop.poc.process.entity.ProcessDeployEntity;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Cache<Long, ProcessDeploy> cache = CacheBuilder.newBuilder().build();

    @Scheduled(fixedDelay = 1000)
    public void load() {
        long lastId = this.lastId;
        List<ProcessDeploy> tempList = new ArrayList<>();

        do {
            QueryWrapper<ProcessDeployEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.gt(ProcessDeployEntity.ID, lastId);
            queryWrapper.last("limit 40");

            List<ProcessDeployEntity> entities = processDeployDao.selectList(queryWrapper);

            // 清空临时数组
            tempList.clear();
            if(CollUtil.isNotEmpty(entities)) {
                for (ProcessDeployEntity entity : entities) {
                    ProcessDeploy processDeploy = new ProcessDeploy();
                    processDeploy.setId(entity.getId());
                    processDeploy.setProcessId(entity.getProcessId());
                    processDeploy.setProcessName(entity.getProcessName());
                    tempList.add(processDeploy);
                }

                ProcessDeployEntity lastProcessDeploy = entities.get(entities.size() - 1);
                lastId = lastProcessDeploy.getId();

                for (ProcessDeploy processDeploy : tempList) {
                    cache.put(processDeploy.getId(), processDeploy);
                }
            }
        } while(CollUtil.isNotEmpty(tempList));

        // 更新最后的下标
        this.lastId = lastId;
    }

    public Map<Long, ProcessDeploy> asMap() {
        return cache.asMap();
    }

}
