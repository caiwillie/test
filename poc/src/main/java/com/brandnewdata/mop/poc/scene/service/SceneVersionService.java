package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.bff.vo.operate.condition.Scene;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.scene.dao.SceneVersionDao;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SceneVersionService implements ISceneVersionService {

    @Resource
    private SceneVersionDao sceneVersionDao;

    @Override
    public SceneVersionDto fetchLatestVersion(Long sceneId) {
        List<SceneVersionDto> sceneVersionDtoList = fetchSceneVersionListBySceneId(sceneId);
        if(CollUtil.isEmpty(sceneVersionDtoList)) return null;
        // 选择状态不是“已停止”的最新版本; 如果状态都是“已停止”，选择最新版本
        Optional<SceneVersionDto> versionOpt = sceneVersionDtoList.stream().filter(sceneVersionPo ->
                !NumberUtil.equals(sceneVersionPo.getStatus(), SceneConst.SCENE_VERSION_STATUS__STOPPED)).findFirst();
        return versionOpt.isPresent() ? versionOpt.get() : sceneVersionDtoList.get(0);
    }

    @Override
    public List<SceneVersionDto> fetchSceneVersionListBySceneId(Long sceneId) {
        Assert.notNull(sceneId, "场景不能为空");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.eq(SceneVersionPo.SCENE_ID, sceneId);
        query.orderByDesc(SceneVersionPo.CREATE_TIME);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().map(po -> new SceneVersionDto().from(po)).collect(Collectors.toList());
    }
}
