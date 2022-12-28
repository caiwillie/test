package com.brandnewdata.mop.poc.scene.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.scene.bo.SceneReleaseVersionBo;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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

    @Override
    public Map<Long, SceneVersionDto> fetchLatestOneBySceneId(List<Long> sceneIdList, List<Integer> statusList) {
        if (CollUtil.isEmpty(sceneIdList)) return MapUtil.empty();
        Map<Long, List<SceneVersionDto>> sceneVersionListMap = fetchListBySceneId(sceneIdList);
        Map<Long, SceneVersionDto> ret = new HashMap<>();

        for (Long sceneId : sceneIdList) {
            List<SceneVersionDto> sceneVersionDtoList = sceneVersionListMap.get(sceneId);
            if (CollUtil.isEmpty(sceneVersionDtoList)) {
                log.error("场景版本数据异常。sceneId：{}", sceneId);
                throw new RuntimeException("【01】场景版本数据异常");
            }
            // 选择状态不是“已停止”的最新版本; 如果状态都是“已停止”，选择最新版本
            Optional<SceneVersionDto> versionOpt = sceneVersionDtoList.stream().filter(sceneVersionDto -> {
                        if (CollUtil.isEmpty(statusList)) return true;
                        else return statusList.contains(sceneVersionDto.getStatus());
                    })
                    .findFirst();
            ret.put(sceneId, versionOpt.orElse(null));
        }
        return ret;
    }

    @Override
    public SceneVersionDto save(SceneVersionDto sceneVersionDto) {
        SceneVersionPo sceneVersionPo = SceneVersionPoConverter.createFrom(sceneVersionDto);
        sceneVersionDao.insert(sceneVersionPo);
        return SceneVersionDtoConverter.createFrom(sceneVersionPo);
    }

    @Override
    public boolean checkNewReleaseVersion(Long sceneId, String version) {
        Assert.notNull(sceneId, "场景id不能为空");
        Assert.notNull(version, "版本号不能为空");
        SceneVersionDto latestSceneVersionDto = fetchLatestOneBySceneId(ListUtil.of(sceneId),
                ListUtil.of(SceneConst.SCENE_VERSION_STATUS__RUNNING, SceneConst.SCENE_VERSION_STATUS__STOPPED)).get(sceneId);
        SceneReleaseVersionBo sceneReleaseVersionBo = parseReleaseVersion(version);
        if(latestSceneVersionDto == null) return true;

        String latestVersion = latestSceneVersionDto.getVersion();
        SceneReleaseVersionBo latestSceneReleaseVersionBo = parseReleaseVersion(latestVersion);

        if(NumberUtil.compare(sceneReleaseVersionBo.getMajor(), latestSceneReleaseVersionBo.getMajor()) < 0) return false;
        if(NumberUtil.compare(sceneReleaseVersionBo.getMajor(), latestSceneReleaseVersionBo.getMajor()) > 0) return true;
        if(NumberUtil.compare(sceneReleaseVersionBo.getMinor(), latestSceneReleaseVersionBo.getMinor()) < 0) return false;
        if(NumberUtil.compare(sceneReleaseVersionBo.getMinor(), latestSceneReleaseVersionBo.getMinor()) > 0) return true;
        if(NumberUtil.compare(sceneReleaseVersionBo.getPatch(), latestSceneReleaseVersionBo.getPatch()) < 0) return false;
        if(NumberUtil.compare(sceneReleaseVersionBo.getPatch(), latestSceneReleaseVersionBo.getPatch()) > 0) return true;
        throw new RuntimeException("版本号不能相同");
    }

    @Override
    public SceneVersionDto fetchByIdAndCheckStatus(Long id, int[] statusArr) {
        Assert.notNull(id, "版本ID不能为空");

        SceneVersionDto versionDto = fetchById(ListUtil.of(id)).get(id);
        Assert.notNull(versionDto, "版本ID不存在。ID: {}", id);

        boolean flag = false;
        for (int status : statusArr) {
            if (NumberUtil.equals(versionDto.getStatus(), status)) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            throw new RuntimeException(StrUtil.format("版本状态异常，id: {}", id));
        } else {
            return versionDto;
        }
    }

    private SceneReleaseVersionBo parseReleaseVersion(String name) {
        Assert.isTrue(ReUtil.isMatch(SceneReleaseVersionBo.PATTERN, name), "版本名称格式不正确");
        List<String> groups = ReUtil.getAllGroups(SceneReleaseVersionBo.PATTERN, name, false);

        SceneReleaseVersionBo bo = new SceneReleaseVersionBo();
        bo.setMajor(Integer.parseInt(groups.get(0)));
        bo.setMinor(Integer.parseInt(groups.get(1)));
        bo.setPatch(Integer.parseInt(groups.get(2)));
        bo.setDate(LocalDateTimeUtil.parseDate(groups.get(3), DatePattern.PURE_DATE_PATTERN));
        return bo;
    }
}
