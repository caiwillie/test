package com.brandnewdata.mop.poc.scene.service.atomic;

import cn.hutool.core.collection.CollUtil;
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
        query.in(VersionProcessPo.ID, idList);

        List<VersionProcessPo> versionProcessPos = versionProcessDao.selectList(query);

        return versionProcessPos.stream().map(VersionProcessDtoConverter::createFrom)
                .collect(Collectors.toMap(VersionProcessDto::getId, Function.identity()));
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
    public Map<String, VersionProcessDto> fetchOneByProcessId(List<String> processIdList) {
        if(CollUtil.isEmpty(processIdList)) return MapUtil.empty();
        Assert.isFalse(CollUtil.hasNull(processIdList), "流程id列表不能含有空值");

        QueryWrapper<VersionProcessPo> query = new QueryWrapper<>();
        query.in(VersionProcessPo.PROCESS_ID, processIdList);

        List<VersionProcessPo> versionProcessPos = versionProcessDao.selectList(query);
        return versionProcessPos.stream().map(VersionProcessDtoConverter::createFrom)
                .collect(Collectors.toMap(VersionProcessDto::getProcessId, Function.identity()));
    }
}
