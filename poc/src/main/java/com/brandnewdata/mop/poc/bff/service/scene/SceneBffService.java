package com.brandnewdata.mop.poc.bff.service.scene;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import com.brandnewdata.mop.poc.bff.converter.scene.SceneDtoConverter;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVersionVo;
import com.brandnewdata.mop.poc.bff.vo.scene.SceneVo;
import com.brandnewdata.mop.poc.bff.vo.scene.VersionProcessVo;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.SceneDto2;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.service.ISceneService2;
import com.brandnewdata.mop.poc.scene.service.ISceneVersionService;
import com.brandnewdata.mop.poc.scene.service.IVersionProcessService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SceneBffService {

    private final ISceneService2 sceneService;

    private final ISceneVersionService sceneVersionService;

    private final IVersionProcessService versionProcessService;

    public SceneBffService(ISceneService2 sceneService,
                           ISceneVersionService sceneVersionService,
                           IVersionProcessService versionProcessService) {
        this.sceneService = sceneService;
        this.sceneVersionService = sceneVersionService;
        this.versionProcessService = versionProcessService;
    }

    public Page<SceneVo> page(Integer pageNum, Integer pageSize, String name) {
        Page<SceneDto2> page = sceneService.page(pageNum, pageSize, name);
        List<SceneDto2> records = page.getRecords();

        if(CollUtil.isEmpty(records)) return new Page<>(page.getTotal(), ListUtil.empty());
        List<SceneVo> sceneVos = new ArrayList<>();

        // 获取所有场景的最新版本
        List<Long> sceneIdList = records.stream().map(SceneDto2::getId).collect(Collectors.toList());
        Map<Long, SceneVersionDto> sceneVersionDtoMap = sceneVersionService.fetchLatestVersion(sceneIdList);
        List<Long> versionIdList = sceneVersionDtoMap.values().stream().map(SceneVersionDto::getId).collect(Collectors.toList());

        // 获取某版本下的流程个数
        Map<Long, Integer> countMap = versionProcessService.fetchVersionProcessCountByVersionIdList(versionIdList);
        // 获取某版本下的最新流程
        Map<Long, VersionProcessDto> processDtoMap = versionProcessService.fetchLatestProcessByVersionIdList(versionIdList);

        for (SceneDto2 sceneDto : records) {
            SceneVo sceneVo = new SceneVo().from(sceneDto);
            Long versionId = sceneVersionDtoMap.get(sceneVo.getId()).getId();
            Integer count = Opt.ofNullable(countMap.get(versionId)).orElse(0);
            String img = Opt.ofNullable(processDtoMap.get(versionId)).map(VersionProcessDto::getProcessImg).orElse(null);
            sceneVo.setProcessCount(count);
            sceneVo.setImgUrl(img);
            sceneVos.add(sceneVo);
        }
        return new Page<>(page.getTotal(), sceneVos);
    }

    public List<SceneVersionVo> versionList(Long sceneId) {
        Assert.notNull(sceneId, "场景id不能为空");
        List<SceneVersionDto> sceneVersionDtoList =
                sceneVersionService.fetchSceneVersionListBySceneId(ListUtil.of(sceneId)).get(sceneId);
        return sceneVersionDtoList.stream()
                .map(dto -> new SceneVersionVo().from(dto)).collect(Collectors.toList());
    }

    public List<VersionProcessVo> processList(Long versionId) {
        Assert.notNull(versionId, "版本id不能为空");
        List<VersionProcessDto> versionProcessDtoList =
                versionProcessService.fetchVersionProcessListByVersionId(ListUtil.of(versionId), false).get(versionId);
        return versionProcessDtoList.stream().map(dto -> new VersionProcessVo().from(dto)).collect(Collectors.toList());
    }

    public SceneVo save(SceneVo vo) {
        SceneDto2 ret = sceneService.save(SceneDtoConverter.createFrom(vo));
        return vo.from(ret);
    }
}
