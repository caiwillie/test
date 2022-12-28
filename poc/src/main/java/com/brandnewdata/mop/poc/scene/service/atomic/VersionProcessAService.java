package com.brandnewdata.mop.poc.scene.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessDtoConverter;
import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VersionProcessAService implements IVersionProcessAService {

    @Resource
    private VersionProcessDao versionProcessDao;

    @Override
    public Map<Long, VersionProcessDto> fetchOneById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(idList), "版本id列表不能含有空值");

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.isNull(VersionProcessPo.DELETE_FLAG);
        query.in(VersionProcessPo.ID, idList);

        List<VersionProcessPo> versionProcessPos = versionProcessDao.selectList(query);

        return versionProcessPos.stream().map(VersionProcessDtoConverter::createFrom)
                .collect(Collectors.toMap(VersionProcessDto::getId, Function.identity()));
    }

    @Override
    public Map<Long, List<VersionProcessDto>> fetchListByVersionId(List<Long> versionIdList, boolean simple) {
        if(CollUtil.isEmpty(versionIdList)) return MapUtil.empty();

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.isNull(VersionProcessPo.DELETE_FLAG);
        query.in(VersionProcessPo.VERSION_ID, versionIdList);
        if(simple) {
            // 不查询xml，img等特大字段
            query.select(VersionProcessPo.class, tableFieldInfo ->
                    !StrUtil.equalsAny(tableFieldInfo.getColumn(), VersionProcessPo.PROCESS_XML, VersionProcessPo.PROCESS_IMG));
        }
        List<VersionProcessPo> versionProcessPos = versionProcessDao.selectList(query);

        return versionProcessPos.stream().collect(Collectors.groupingBy(VersionProcessPo::getVersionId,
                Collectors.mapping(VersionProcessDtoConverter::createFrom, Collectors.toList())));
    }

    @Override
    public Map<String, VersionProcessDto> fetchOneByProcessId(List<String> processIdList) {
        if(CollUtil.isEmpty(processIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(processIdList), "流程id列表不能含有空值");

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.isNull(VersionProcessPo.DELETE_FLAG);
        query.in(VersionProcessPo.PROCESS_ID, processIdList);

        List<VersionProcessPo> versionProcessPos = versionProcessDao.selectList(query);
        return versionProcessPos.stream().map(VersionProcessDtoConverter::createFrom)
                .collect(Collectors.toMap(VersionProcessDto::getProcessId, Function.identity()));
    }

    @Override
    public Map<Long, Integer> fetchCountByVersionId(List<Long> versionIdList) {
        if(CollUtil.isEmpty(versionIdList)) return MapUtil.empty();
        Map<Long, Integer> ret = new HashMap<>();
        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.isNull(VersionProcessPo.DELETE_FLAG);
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
    public Map<Long, VersionProcessDto> fetchLatestOneByVersionId(List<Long> versionIdList) {
        Map<Long, List<VersionProcessDto>> versionProcessListMap = fetchListByVersionId(versionIdList, true);
        Map<Long, Long> idMap = versionProcessListMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            List<VersionProcessDto> versionProcessDtos = entry.getValue();
            return versionProcessDtos.stream().max(Comparator.comparing(VersionProcessDto::getUpdateTime))
                    .map(VersionProcessDto::getId).orElse(-1L);
        }));
        Map<Long, VersionProcessDto> versionProcessMap = fetchOneById(ListUtil.toList(idMap.values()));
        return idMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> versionProcessMap.get(entry.getValue())));
    }
}
