package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.scene.converter.ScenePoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneDao;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.ScenePo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SceneService2 implements ISceneService2{

    @Resource
    private SceneDao sceneDao;

    private final ISceneVersionService sceneVersionService;

    public SceneService2(ISceneVersionService sceneVersionService) {
        this.sceneVersionService = sceneVersionService;
    }

    @Override
    public Page<SceneDto2> page(int pageNum, int pageSize, String name) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ScenePo> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNum, pageSize);
        QueryWrapper<ScenePo> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(name)) queryWrapper.like(ScenePo.NAME, name); // 设置名称
        queryWrapper.orderByDesc(ScenePo.UPDATE_TIME);
        page = sceneDao.selectPage(page, queryWrapper);
        List<ScenePo> scenePoList = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty());
        List<SceneDto2> records = scenePoList.stream().map(po -> new SceneDto2().from(po)).collect(Collectors.toList());
        return new Page<>(page.getTotal(), records);
    }

    @Override
    public SceneDto2 save(SceneDto2 sceneDto) {
        Long sceneId = sceneDto.getId();
        ScenePo scenePo = null;
        if(sceneId == null) {
            // 新增场景
            scenePo = ScenePoConverter.createFrom(sceneDto);
            sceneDao.insert(scenePo);

            // 新增初始化版本
            SceneVersionDto sceneVersionDto = new SceneVersionDto();
            sceneVersionDto.setSceneId(scenePo.getId());
            sceneVersionDto.setVersion(DateUtil.format(scenePo.getCreateTime(), DatePattern.PURE_DATETIME_PATTERN));
            sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__CONFIG);
            sceneVersionService.save(sceneVersionDto);

        } else {
            scenePo = getScenePoById(sceneId);
            ScenePoConverter.updateFrom(sceneDto, scenePo);
            sceneDao.updateById(scenePo);
        }
        return new SceneDto2().from(scenePo);
    }

    private ScenePo getScenePoById(Long sceneId) {
        Assert.notNull(sceneId, "场景id不能为空");
        ScenePo scenePo = sceneDao.selectById(sceneId);
        Assert.notNull(scenePo, "场景id不存在");
        return scenePo;
    }

}
