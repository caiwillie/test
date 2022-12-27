package com.brandnewdata.mop.poc.scene.service.atomic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
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
}
