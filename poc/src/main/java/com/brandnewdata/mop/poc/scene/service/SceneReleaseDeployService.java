package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.scene.converter.SceneReleaseDeployDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.SceneReleaseDeployPoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneReleaseDeployDao;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.po.SceneReleaseDeployPo;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

public class SceneReleaseDeployService implements ISceneReleaseDeployService {

    @Resource
    private SceneReleaseDeployDao sceneReleaseDeployDao;

    @Override
    public SceneReleaseDeployDto save(SceneReleaseDeployDto dto) {
        String processId = dto.getProcessId();
        Long envId = dto.getEnvId();

        // 判断是否存在
        QueryWrapper<SceneReleaseDeployPo> query = new QueryWrapper<>();
        query.eq(SceneReleaseDeployPo.ENV_ID, envId);
        query.eq(SceneReleaseDeployPo.PROCESS_ID, processId);
        Long count = sceneReleaseDeployDao.selectCount(query);
        Assert.isTrue(count == 0, "版本已经发布");

        dto.setId(IdUtil.getSnowflakeNextId());
        sceneReleaseDeployDao.insert(SceneReleaseDeployPoConverter.createFrom(dto));

        return dto;
    }

    @Override
    public List<SceneReleaseDeployDto> fetchByEnvId(Long envId) {
        QueryWrapper<SceneReleaseDeployPo> query = new QueryWrapper<>();
        query.eq(SceneReleaseDeployPo.ENV_ID, envId);
        List<SceneReleaseDeployPo> sceneReleaseDeployPoList = sceneReleaseDeployDao.selectList(query);

        return sceneReleaseDeployPoList.stream().map(SceneReleaseDeployDtoConverter::createFrom).collect(Collectors.toList());
    }
}
