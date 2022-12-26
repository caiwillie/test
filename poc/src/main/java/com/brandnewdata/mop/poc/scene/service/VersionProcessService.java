package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.dto.ProcessDefinitionParseDto;
import com.brandnewdata.mop.poc.process.service.IProcessDefinitionService2;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.VersionProcessPoConverter;
import com.brandnewdata.mop.poc.scene.dao.VersionProcessDao;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.manager.JooqManager;
import com.brandnewdata.mop.poc.scene.po.VersionProcessPo;
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
    private JooqManager jooqManager;

    @Resource
    private VersionProcessDao versionProcessDao;

    private final IProcessDefinitionService2 processDefinitionService;

    public VersionProcessService(IProcessDefinitionService2 processDefinitionService) {
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public Map<Long, List<VersionProcessDto>> fetchListByVersionId(List<Long> versionIdList, boolean simple) {
        if(CollUtil.isEmpty(versionIdList)) return MapUtil.empty();

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
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
    public Map<Long, VersionProcessDto> fetchOneById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(idList), "版本id列表不能含有空值");

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.in(VersionProcessPo.ID, idList);

        List<VersionProcessPo> versionProcessPos = versionProcessDao.selectList(query);

        return versionProcessPos.stream().map(VersionProcessDtoConverter::createFrom)
                .collect(Collectors.toMap(VersionProcessDto::getId, Function.identity()));
    }

    @Override
    public Map<String, VersionProcessDto> fetchOneByProcessId(List<String> processIdList) {
        if(CollUtil.isEmpty(processIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(processIdList), "流程id列表不能含有空值");

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
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

    @Override
    public VersionProcessDto save(VersionProcessDto versionProcessDto) {

        BpmnXmlDto bpmnXmlDto = new BpmnXmlDto();
        bpmnXmlDto.setProcessId(versionProcessDto.getProcessId());
        bpmnXmlDto.setProcessName(versionProcessDto.getProcessName());
        bpmnXmlDto.setProcessXml(versionProcessDto.getProcessXml());
        bpmnXmlDto = processDefinitionService.replaceProcessId(bpmnXmlDto);

        String processId = bpmnXmlDto.getProcessId();
        String name = bpmnXmlDto.getProcessName();
        String processXml = bpmnXmlDto.getProcessXml();

        Long id = versionProcessDto.getId();

        if(id == null) {
            // 手动指定
            versionProcessDto.setId(IdUtil.getSnowflakeNextId());
            versionProcessDto.setProcessId(processId);
            versionProcessDto.setProcessName(name);

            VersionProcessPo versionProcessPo = VersionProcessPoConverter.createFrom(versionProcessDto);
            versionProcessPo.setProcessXml(processXml);
            versionProcessDao.insert(versionProcessPo);
        } else {
            VersionProcessDto updateContent = versionProcessDto;
            versionProcessDto = fetchOneById(ListUtil.of(id)).get(id);
            if(!StrUtil.equals(versionProcessDto.getProcessId(), processId)) {
                throw new RuntimeException("流程id不能改变");
            }
            versionProcessDto.setProcessName(name);
            versionProcessDto.setProcessXml(processXml);
            versionProcessDto.setProcessImg(updateContent.getProcessImg());

            VersionProcessPo versionProcessPo = VersionProcessPoConverter.createFrom(versionProcessDto);
            versionProcessPo.setProcessXml(processXml);
            versionProcessDao.updateById(versionProcessPo);
        }

        return versionProcessDto;
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
