package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.scene.dao.SceneVersionDao;
import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.manager.JooqManager;
import com.brandnewdata.mop.poc.scene.po.SceneVersionPo;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VersionProcessService implements IVersionProcessService {

    @Resource
    private JooqManager jooqManager;

    @Resource
    private VersionProcessDao versionProcessDao;

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
    public Map<Long, Integer> fetchVersionProcessCountByVersionIdList(List<Long> versionIdList) {
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
