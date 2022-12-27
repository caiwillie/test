package com.brandnewdata.mop.poc.scene.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.scene.converter.SceneVersionDtoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneVersionDao;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;
import com.brandnewdata.mop.poc.util.CollectorsUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SceneVersionAService implements ISceneVersionAService {
    @Resource
    private SceneVersionDao sceneVersionDao;

    @Override
    public Map<Long, Long> countById(List<Long> idList) {
        if (CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isTrue(idList.stream().filter(Objects::isNull).count() == 0, "版本id不能为空");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.ID, idList);
        query.select(SceneVersionPo.ID, "count(*) as num");
        query.groupBy(SceneVersionPo.ID);
        List<Map<String, Object>> maps = sceneVersionDao.selectMaps(query);
        Map<Long, Long> countMap = maps.stream().collect(
                Collectors.toMap(map -> (Long) map.get(SceneVersionPo.ID), map -> (Long) map.get("num")));

        // 将未找到的scene id的个数设置为0
        return idList.stream().collect(
                Collectors.toMap(Function.identity(), key -> Opt.ofNullable(countMap.get(key)).orElse(0L)));
    }

    @Override
    public Map<Long, SceneVersionDto> fetchById(List<Long> idList) {
        if (CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isTrue(idList.stream().filter(Objects::isNull).count() == 0, "版本id不能为空");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.ID, idList);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().map(SceneVersionDtoConverter::createFrom)
                .collect(Collectors.toMap(SceneVersionDto::getId, Function.identity()));
    }

    @Override
    public Map<Long, List<SceneVersionDto>> fetchListBySceneId(List<Long> sceneIdList) {
        if (CollUtil.isEmpty(sceneIdList)) return MapUtil.empty();
        long count = sceneIdList.stream().filter(Objects::isNull).count();
        Assert.isTrue(count == 0, "场景id列表不能存在空值");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.SCENE_ID, sceneIdList);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().collect(Collectors.groupingBy(SceneVersionPo::getSceneId,
                Collectors.mapping(SceneVersionDtoConverter::createFrom,
                        CollectorsUtil.toSortedList((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime())))));
    }

    @Override
    public List<SceneVersionDto> fetchAll() {
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().map(SceneVersionDtoConverter::createFrom).collect(Collectors.toList());
    }
}
