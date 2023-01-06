package com.brandnewdata.mop.poc.scene.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
        query.isNull(SceneReleaseDeployPo.DELETE_FLAG);
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
        query.isNull(SceneReleaseDeployPo.DELETE_FLAG);
        query.eq(SceneReleaseDeployPo.ENV_ID, envId);
        List<SceneReleaseDeployPo> sceneReleaseDeployPoList = sceneReleaseDeployDao.selectList(query);

        return sceneReleaseDeployPoList.stream().map(SceneReleaseDeployDtoConverter::createFrom).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<SceneReleaseDeployDto>> fetchListByVersionId(List<Long> versionIdList) {
        if(CollUtil.isEmpty(versionIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(versionIdList), "processIdList中存在null值");

        QueryWrapper<SceneReleaseDeployPo> query = new QueryWrapper<>();
        query.isNull(SceneReleaseDeployPo.DELETE_FLAG);
        query.in(SceneReleaseDeployPo.VERSION_ID, versionIdList);

        List<SceneReleaseDeployPo> sceneReleaseDeployPoList = sceneReleaseDeployDao.selectList(query);
        return sceneReleaseDeployPoList.stream().map(SceneReleaseDeployDtoConverter::createFrom)
                .collect(Collectors.groupingBy(SceneReleaseDeployDto::getVersionId));
    }

    @Override
    public void deleteByVersionId(Long versionId) {
        Assert.notNull(versionId);
        UpdateWrapper<SceneReleaseDeployPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{}={}", SceneReleaseDeployPo.DELETE_FLAG, SceneReleaseDeployPo.ID));
        update.eq(SceneReleaseDeployPo.VERSION_ID, versionId);
        sceneReleaseDeployDao.update(null, update);
    }

    @Override
    public void deleteBySceneId(Long sceneId) {
        Assert.notNull(sceneId);
        UpdateWrapper<SceneReleaseDeployPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{}={}", SceneReleaseDeployPo.DELETE_FLAG, SceneReleaseDeployPo.ID));
        update.eq(SceneReleaseDeployPo.SCENE_ID, sceneId);
        sceneReleaseDeployDao.update(null, update);
    }

    @Override
    public void deleteByVersionIdAndExceptEnvId(Long versionId, List<Long> envIdList) {
        Assert.notNull(versionId);
        Assert.notEmpty(envIdList);
        Assert.isFalse(CollUtil.hasNull(envIdList), "envIdList can not contain null");
        UpdateWrapper<SceneReleaseDeployPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{}={}", SceneReleaseDeployPo.DELETE_FLAG, SceneReleaseDeployPo.ID));
        update.eq(SceneReleaseDeployPo.VERSION_ID, versionId);
        update.in(SceneReleaseDeployPo.ENV_ID, envIdList);
        sceneReleaseDeployDao.update(null, update);
    }
}
