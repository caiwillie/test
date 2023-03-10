package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.scene.converter.SceneDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.ScenePoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneDao;
import com.brandnewdata.mop.poc.scene.dto.SceneDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.ScenePo;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneReleaseDeployAService;
import com.brandnewdata.mop.poc.scene.service.atomic.ISceneVersionAService;
import com.brandnewdata.mop.poc.scene.service.combine.ISceneVersionCService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SceneService implements ISceneService {

    @Resource
    private SceneDao sceneDao;

    private final ISceneVersionAService sceneVersionAService;

    private final ISceneVersionCService sceneVersionCService;

    private final ISceneReleaseDeployAService sceneReleaseDeployAService;

    public SceneService(ISceneVersionAService sceneVersionAService,
                        ISceneVersionCService sceneVersionCService,
                        ISceneReleaseDeployAService sceneReleaseDeployAService) {
        this.sceneVersionAService = sceneVersionAService;
        this.sceneVersionCService = sceneVersionCService;
        this.sceneReleaseDeployAService = sceneReleaseDeployAService;
    }

    @Override
    public Page<SceneDto> page(long projectId, int pageNum, int pageSize, String name) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ScenePo> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize);
        QueryWrapper<ScenePo> query = new QueryWrapper<>();
        query.isNull(ScenePo.DELETE_FLAG);
        query.eq(ScenePo.PROJECT_ID, projectId);
        if(StrUtil.isNotBlank(name)) query.like(ScenePo.NAME, name); // ????????????
        query.orderByDesc(ScenePo.UPDATE_TIME);
        page = sceneDao.selectPage(page, query);
        List<ScenePo> scenePoList = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty());
        List<SceneDto> records = scenePoList.stream().map(SceneDtoConverter::createFrom).collect(Collectors.toList());
        return new Page<>(page.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SceneDto save(SceneDto sceneDto) {
        Long sceneId = sceneDto.getId();
        ScenePo scenePo = null;
        if(sceneId == null) {
            // ????????????
            scenePo = ScenePoConverter.createFrom(sceneDto);
            sceneDao.insert(scenePo);

            // ?????????????????????
            SceneVersionDto sceneVersionDto = new SceneVersionDto();
            sceneVersionDto.setSceneId(scenePo.getId());
            sceneVersionDto.setVersion(DateUtil.format(scenePo.getCreateTime(), DatePattern.PURE_DATETIME_PATTERN));
            sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__CONFIGURING);
            sceneVersionDto.setDeployStatus(SceneConst.SCENE_DEPLOY_STATUS__DEPLOYED);
            sceneVersionAService.save(sceneVersionDto);

        } else {
            scenePo = getScenePoById(sceneId);
            ScenePoConverter.updateFrom(sceneDto, scenePo);
            sceneDao.updateById(scenePo);
        }
        return SceneDtoConverter.createFrom(scenePo);
    }

    @Override
    public Map<Long, SceneDto> fetchById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(idList), "??????id??????????????????");

        QueryWrapper<ScenePo> query = new QueryWrapper<>();
        query.isNull(ScenePo.DELETE_FLAG);
        query.in(ScenePo.ID, idList);

        return sceneDao.selectList(query).stream().map(SceneDtoConverter::createFrom)
                .collect(Collectors.toMap(SceneDto::getId, Function.identity()));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        getScenePoById(id);

        // ????????????????????????
        sceneVersionCService.deleteBySceneId(id);

        // ????????????????????????
        sceneReleaseDeployAService.deleteBySceneId(id);

        // ????????????
        UpdateWrapper<ScenePo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{}={}", ScenePo.DELETE_FLAG, ScenePo.ID));
        update.eq(ScenePo.ID, id);
        sceneDao.update(null, update);
    }

    private ScenePo getScenePoById(Long sceneId) {
        Assert.notNull(sceneId, "??????id????????????");
        ScenePo scenePo = sceneDao.selectById(sceneId);
        Assert.notNull(scenePo, "??????id?????????");
        return scenePo;
    }

}
