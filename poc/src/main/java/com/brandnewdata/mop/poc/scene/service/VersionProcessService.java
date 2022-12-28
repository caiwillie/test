package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessDtoConverter;
import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VersionProcessService implements IVersionProcessService {

    @Resource
    private VersionProcessDao versionProcessDao;

    private final IVersionProcessAService versionProcessAService;

    public VersionProcessService(IVersionProcessAService versionProcessAService) {
        this.versionProcessAService = versionProcessAService;
    }





    @Override
    public Map<Long, Integer> fetchCountByVersionId(List<Long> versionIdList) {
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
    public Map<Long, VersionProcessDto> fetchLatestOneByVersionId(List<Long> versionIdList) {
        Map<Long, List<VersionProcessDto>> versionProcessListMap = versionProcessAService.fetchListByVersionId(versionIdList, true);
        Map<Long, Long> idMap = versionProcessListMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            List<VersionProcessDto> versionProcessDtos = entry.getValue();
            return versionProcessDtos.stream().max(Comparator.comparing(VersionProcessDto::getUpdateTime))
                    .map(VersionProcessDto::getId).orElse(-1L);
        }));
        Map<Long, VersionProcessDto> versionProcessMap = versionProcessAService.fetchOneById(ListUtil.toList(idMap.values()));
        return idMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> versionProcessMap.get(entry.getValue())));
    }

    @Override
    public void deleteById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return;
        Assert.isFalse(CollUtil.hasNull(idList), "版本id列表不能含有空值");
        for (Long id : idList) {
            versionProcessDao.deleteById(id);
        }
    }



}
