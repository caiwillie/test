package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dao.SceneDao;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.po.ScenePo;
import com.brandnewdata.mop.poc.scene.po.ScenePoExt;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SceneService2 implements ISceneService2{

    @Resource
    private SceneDao sceneDao;

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
        ScenePo scenePo = getScenePoById(sceneId);
        if(scenePo == null) {
            scenePo = new ScenePoExt().createFrom(sceneDto);
        } else {

        }
        return null;
    }

    private ScenePo getScenePoById(Long sceneId) {
        Assert.notNull(sceneId, "场景id不能为空");
        return sceneDao.selectById(sceneId);
    }

}
