package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.scene.dao.SceneProcessDao;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto;
import com.brandnewdata.mop.poc.scene.po.SceneProcessPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SceneProcessService implements ISceneProcessService{

    @Resource
    private SceneProcessDao sceneProcessDao;

    @Override
    public List<SceneProcessDto> listByProcessIdList(List<String> processIdList) {
        if(CollUtil.isEmpty(processIdList)) return ListUtil.empty();
        QueryWrapper<SceneProcessPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(SceneProcessPo.PROCESS_ID, processIdList);
        List<SceneProcessPo> entities = sceneProcessDao.selectList(queryWrapper);
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SceneProcessDto> listBySceneIdList(List<Long> sceneIdList) {
        if(CollUtil.isEmpty(sceneIdList)) return ListUtil.empty();
        QueryWrapper<SceneProcessPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(SceneProcessPo.BUSINESS_SCENE_ID, sceneIdList);
        List<SceneProcessPo> entities = sceneProcessDao.selectList(queryWrapper);
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }


    private SceneProcessDto toDto(SceneProcessPo entity) {
        SceneProcessDto dto = new SceneProcessDto();
        dto.setId(entity.getId());
        dto.setProcessId(entity.getProcessId());
        dto.setSceneId(entity.getBusinessSceneId());
        return dto;
    }
}
