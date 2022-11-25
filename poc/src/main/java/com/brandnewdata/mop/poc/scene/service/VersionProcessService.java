package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionStaticParseDto;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService2;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessPoConverter;
import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.manager.JooqManager;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VersionProcessService implements IVersionProcessService {

    @Resource
    private JooqManager jooqManager;

    @Resource
    private VersionProcessDao versionProcessDao;

    private final IProcessDefinitionService2 processDefinitionService;

    public VersionProcessService(IProcessDefinitionService2 processDefinitionService) {
        this.processDefinitionService = processDefinitionService;
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
                Collectors.mapping(VersionProcessDtoConverter::from, Collectors.toList())));
    }

    @Override
    public Map<Long, VersionProcessDto> fetchVersionProcessById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        if(idList.stream().filter(Objects::isNull).count() > 0) {
            log.error("版本id列表不能含有空值。idList：{}", JacksonUtil.to(idList));
            throw new RuntimeException("版本id列表不能含有空值");
        }

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.in(VersionProcessPo.ID, idList);

        List<VersionProcessPo> versionProcessPos = versionProcessDao.selectList(query);

        return versionProcessPos.stream().map(VersionProcessDtoConverter::from)
                .collect(Collectors.toMap(VersionProcessDto::getId, Function.identity()));
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

    @Override
    public VersionProcessDto save(VersionProcessDto versionProcessDto) {
        String processXml = versionProcessDto.getProcessXml();
        BizDeployDto bizDeployDto = new BizDeployDto();
        bizDeployDto.setProcessId(null);
        bizDeployDto.setProcessName(null);
        bizDeployDto.setProcessXml(processXml);
        ProcessDefinitionStaticParseDto processDefinitionStaticParseDto = processDefinitionService.staticParse(bizDeployDto);

        String processId = processDefinitionStaticParseDto.getProcessId();
        String name = processDefinitionStaticParseDto.getName();

        Long id = versionProcessDto.getId();
        if(id == null) {
            // 手动指定
            versionProcessDto.setId(IdUtil.getSnowflakeNextId());
            versionProcessDto.setProcessId(processId);
            versionProcessDto.setProcessName(name);
            versionProcessDao.insert(VersionProcessPoConverter.createFrom(versionProcessDto));
        } else {
            versionProcessDto = fetchVersionProcessById(ListUtil.of(id)).get(id);
            if(!StrUtil.equals(versionProcessDto.getProcessId(), processId)) {
                throw new RuntimeException("流程id不能改变");
            }
            versionProcessDto.setProcessName(name);
            versionProcessDao.updateById(VersionProcessPoConverter.createFrom(versionProcessDto));
        }

        return versionProcessDto;
    }


}
