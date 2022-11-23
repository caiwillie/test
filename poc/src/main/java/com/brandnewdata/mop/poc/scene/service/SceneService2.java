package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.scene.dao.SceneDao;
import com.brandnewdata.mop.poc.scene.dao.SceneVersionDao;
import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.manager.JooqManager;
import com.brandnewdata.mop.poc.scene.po.ScenePo;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SceneService2 implements ISceneService2{

    @Resource
    private SceneDao sceneDao;

    @Resource
    private SceneVersionDao sceneVersionDao;

    @Resource
    private JooqManager jooqManager;

    @Resource
    private VersionProcessDao versionProcessDao;

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
    public SceneVersionDto fetchLatestVersion(Long sceneId) {
        Assert.notNull(sceneId, "场景不能为空");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.eq(SceneVersionPo.SCENE_ID, sceneId);
        query.orderByDesc(SceneVersionPo.CREATE_TIME);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        // 选择状态不是“已停止”的最新版本; 如果状态都是“已停止”，选择最新版本
        Optional<SceneVersionPo> versionOpt = sceneVersionPos.stream().filter(sceneVersionPo ->
                !NumberUtil.equals(sceneVersionPo.getStatus(), SceneConst.SCENE_VERSION_STATUS__STOPPED)).findFirst();
        SceneVersionPo sceneVersionPo = versionOpt.isPresent() ? versionOpt.get() : sceneVersionPos.get(0);
        return new SceneVersionDto().from(sceneVersionPo);
    }

    @Override
    public Map<Long, List<VersionProcessDto>> fetchVersionProcessListByVersionId(List<Long> versionIdList, boolean simple) {
        if(CollUtil.isEmpty(versionIdList)) return MapUtil.empty();
        Map<Long, List<VersionProcessDto>> ret = new HashMap<>();

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.in(VersionProcessPo.VERSION_ID, versionIdList);
        if(simple) {
            // 不查询xml，img等特大字段
            query.select(VersionProcessPo.class, tableFieldInfo ->
                    !StrUtil.equalsAny(tableFieldInfo.getColumn(), VersionProcessPo.PROCESS_XML, VersionProcessPo.PROCESS_IMG));
        }
        List<VersionProcessPo> versionProcessPos = versionProcessDao.selectList(query);

        return versionProcessPos.stream().collect(Collectors.groupingBy(VersionProcessPo::getVersionId,
                Collectors.mapping(po -> new VersionProcessDto().from(po), Collectors.toList())));
    }

    @Override
    public Map<Long, VersionProcessDto> fetchVersionProcessById(List<Long> idList) {
        return null;
    }

    @Override
    public Map<Long, Integer> fetchProcessCountByVersionIdList(List<Long> versionIdList) {
        if(CollUtil.isEmpty(versionIdList)) return MapUtil.empty();
        Map<Long, Integer> ret = new HashMap<>();
        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.in(VersionProcessPo.VERSION_ID, versionIdList);
        query.select(VersionProcessPo.VERSION_ID, "count(*) as num");
        query.groupBy(VersionProcessPo.VERSION_ID);
        List<Map<String, Object>> records = versionProcessDao.selectMaps(query);

        for (Map<String, Object> record : records) {
            Long versionId = (Long) record.get(VersionProcessPo.VERSION_ID);
            Long num = (Long) record.get("num");
            ret.put(versionId, num.intValue());
        }

        return ret;
    }

    @Override
    public Map<Long, VersionProcessDto> fetchLatestProcessByVersionIdList(List<Long> versionIdList) {
        Map<Long, List<VersionProcessDto>> versionProcessListMap = fetchVersionProcessListByVersionId(versionIdList, true);
        Map<Long, Long> idMap = versionProcessListMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            List<VersionProcessDto> versionProcessDtos = entry.getValue();
            return versionProcessDtos.stream().max(Comparator.comparing(VersionProcessDto::getUpdateTime))
                    .map(VersionProcessDto::getId).orElse(-1L);
        }));
        Map<Long, VersionProcessDto> versionProcessMap = fetchVersionProcessById(ListUtil.toList(idMap.values()));
        return idMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> versionProcessMap.get(entry.getValue())));
    }

}
