package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.scene.dao.SceneProcessDao;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto2;
import com.brandnewdata.mop.poc.scene.entity.SceneProcessEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SceneProcessService implements ISceneProcessService{

    @Resource
    private SceneProcessDao sceneProcessDao;

    @Override
    public List<SceneProcessDto2> listByProcessIdList(List<String> processIdList) {
        if(CollUtil.isEmpty(processIdList)) return ListUtil.empty();
        QueryWrapper<SceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(SceneProcessEntity.PROCESS_ID, processIdList);
        List<SceneProcessEntity> entities = sceneProcessDao.selectList(queryWrapper);
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SceneProcessDto2> listBySceneIdList(List<Long> sceneIdList) {
        if(CollUtil.isEmpty(sceneIdList)) return ListUtil.empty();
        QueryWrapper<SceneProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(SceneProcessEntity.BUSINESS_SCENE_ID, sceneIdList);
        List<SceneProcessEntity> entities = sceneProcessDao.selectList(queryWrapper);
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }


    private SceneProcessDto2 toDto(SceneProcessEntity entity) {
        SceneProcessDto2 dto = new SceneProcessDto2();
        dto.setId(entity.getId());
        dto.setProcessId(entity.getProcessId());
        dto.setSceneId(entity.getBusinessSceneId());
        return dto;
    }
}
