package com.brandnewdata.mop.poc.scene.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.scene.converter.SceneReleaseDeployDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.SceneReleaseDeployPoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneReleaseDeployDao;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.po.SceneReleaseDeployPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SceneReleaseDeployAService implements ISceneReleaseDeployAService {

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
        SceneReleaseDeployPo sceneReleaseDeployPo = sceneReleaseDeployDao.selectOne(query);
        if(sceneReleaseDeployPo == null) {
            dto.setId(IdUtil.getSnowflakeNextId());
            sceneReleaseDeployDao.insert(SceneReleaseDeployPoConverter.createFrom(dto));
        } else {
            dto.setId(sceneReleaseDeployPo.getId());
            SceneReleaseDeployPoConverter.updateFrom(sceneReleaseDeployPo, dto);
            sceneReleaseDeployDao.updateById(sceneReleaseDeployPo);
        }

        return dto;
    }

    @Override
    public List<SceneReleaseDeployDto> fetchByEnvId(Long envId) {
        QueryWrapper<SceneReleaseDeployPo> query = new QueryWrapper<>();
        query.eq(SceneReleaseDeployPo.ENV_ID, envId);
        List<SceneReleaseDeployPo> sceneReleaseDeployPoList = sceneReleaseDeployDao.selectList(query);

        return sceneReleaseDeployPoList.stream().map(SceneReleaseDeployDtoConverter::createFrom).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<SceneReleaseDeployDto>> fetchListByVersionId(List<Long> versionIdList) {
        if(CollUtil.isEmpty(versionIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(versionIdList), "processIdList中存在null值");

        QueryWrapper<SceneReleaseDeployPo> query = new QueryWrapper<>();
        query.in(SceneReleaseDeployPo.VERSION_ID, versionIdList);

        List<SceneReleaseDeployPo> sceneReleaseDeployPoList = sceneReleaseDeployDao.selectList(query);
        return sceneReleaseDeployPoList.stream().map(SceneReleaseDeployDtoConverter::createFrom)
                .collect(Collectors.groupingBy(SceneReleaseDeployDto::getVersionId));
    }
}
