package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.scene.converter.SceneVersionDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.SceneVersionPoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneVersionDao;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;
import com.brandnewdata.mop.poc.util.CollectorsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SceneVersionService implements ISceneVersionService {

    @Resource
    private SceneVersionDao sceneVersionDao;


    @Override
    public Map<Long, SceneVersionDto> fetchLatestVersion(List<Long> sceneIdList) {
        if(CollUtil.isEmpty(sceneIdList)) return MapUtil.empty();
        Map<Long, List<SceneVersionDto>> sceneVersionListMap = fetchSceneVersionListBySceneId(sceneIdList);
        Map<Long, SceneVersionDto> ret = new HashMap<>();
        for (Long sceneId : sceneIdList) {
            List<SceneVersionDto> sceneVersionDtoList = sceneVersionListMap.get(sceneId);
            if(CollUtil.isEmpty(sceneVersionDtoList)) {
                log.error("场景版本数据异常。sceneId：{}", sceneId);
                throw new RuntimeException("【01】场景版本数据异常");
            }
            // 选择状态不是“已停止”的最新版本; 如果状态都是“已停止”，选择最新版本
            Optional<SceneVersionDto> versionOpt = sceneVersionDtoList.stream().filter(sceneVersionPo ->
                    !NumberUtil.equals(sceneVersionPo.getStatus(), SceneConst.SCENE_VERSION_STATUS__STOPPED)).findFirst();
            ret.put(sceneId, versionOpt.orElseGet(() -> sceneVersionDtoList.get(0)));
        }
        return ret;
    }

    @Override
    public Map<Long, List<SceneVersionDto>> fetchSceneVersionListBySceneId(List<Long> sceneIdList) {
        if(CollUtil.isEmpty(sceneIdList)) return MapUtil.empty();
        long count = sceneIdList.stream().filter(Objects::isNull).count();
        Assert.isTrue(count == 0, "场景id列表不能存在空值");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.SCENE_ID, sceneIdList);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().collect(Collectors.groupingBy(SceneVersionPo::getSceneId,
                Collectors.mapping(SceneVersionDtoConverter::from,
                        CollectorsUtil.toSortedList((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime())))));
    }

    @Override
    public SceneVersionDto save(SceneVersionDto sceneVersionDto) {
        SceneVersionPo sceneVersionPo = SceneVersionPoConverter.createFrom(sceneVersionDto);
        sceneVersionDao.insert(sceneVersionPo);
        return SceneVersionDtoConverter.from(sceneVersionPo);
    }

}
