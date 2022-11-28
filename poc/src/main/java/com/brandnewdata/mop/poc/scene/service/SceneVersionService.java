package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.constant.SceneConst;
import com.brandnewdata.mop.poc.env.service.IEnvService;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService;
import com.brandnewdata.mop.poc.process.dto.BizDeployDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.scene.converter.SceneVersionDtoConverter;
import com.brandnewdata.mop.poc.scene.converter.SceneVersionPoConverter;
import com.brandnewdata.mop.poc.scene.dao.SceneVersionDao;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
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
public class SceneVersionService implements ISceneVersionService {

    @Resource
    private SceneVersionDao sceneVersionDao;

    private final IEnvService envService;

    private final IVersionProcessService versionProcessService;

    private final IProcessDeployService2 processDeployService;

    private final IProcessInstanceService processInstanceService;

    public SceneVersionService(IEnvService envService,
                               IVersionProcessService versionProcessService,
                               IProcessDeployService2 processDeployService,
                               IProcessInstanceService processInstanceService) {
        this.envService = envService;
        this.versionProcessService = versionProcessService;
        this.processDeployService = processDeployService;
        this.processInstanceService = processInstanceService;
    }

    @Override
    public Map<Long, SceneVersionDto> fetchLatestVersion(List<Long> sceneIdList) {
        if(CollUtil.isEmpty(sceneIdList)) return MapUtil.empty();
        Map<Long, List<SceneVersionDto>> sceneVersionListMap = fetchSceneVersionListBySceneId(sceneIdList);
        Map<Long, SceneVersionDto> ret = new HashMap<>();
        for (Long sceneId : sceneIdList) {
            List<SceneVersionDto> sceneVersionDtoList = sceneVersionListMap.get(sceneId);
            if(CollUtil.isEmpty(sceneVersionDtoList)) {
                log.error("场景版本数据异常。sceneId：{}", sceneId);
                throw new RuntimeException("【01】场景版本数据异常");
            }
            // 选择状态不是“已停止”的最新版本; 如果状态都是“已停止”，选择最新版本
            Optional<SceneVersionDto> versionOpt = sceneVersionDtoList.stream().filter(sceneVersionPo ->
                    !NumberUtil.equals(sceneVersionPo.getStatus(), SceneConst.SCENE_VERSION_STATUS__STOPPED)).findFirst();
            ret.put(sceneId, versionOpt.orElseGet(() -> sceneVersionDtoList.get(0)));
        }
        return ret;
    }

    @Override
    public Map<Long, List<SceneVersionDto>> fetchSceneVersionListBySceneId(List<Long> sceneIdList) {
        if(CollUtil.isEmpty(sceneIdList)) return MapUtil.empty();
        long count = sceneIdList.stream().filter(Objects::isNull).count();
        Assert.isTrue(count == 0, "场景id列表不能存在空值");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.SCENE_ID, sceneIdList);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().collect(Collectors.groupingBy(SceneVersionPo::getSceneId,
                Collectors.mapping(SceneVersionDtoConverter::from,
                        CollectorsUtil.toSortedList((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime())))));
    }

    @Override
    public SceneVersionDto save(SceneVersionDto sceneVersionDto) {
        SceneVersionPo sceneVersionPo = SceneVersionPoConverter.createFrom(sceneVersionDto);
        sceneVersionDao.insert(sceneVersionPo);
        return SceneVersionDtoConverter.from(sceneVersionPo);
    }

    @Override
    public VersionProcessDto saveProcess(VersionProcessDto dto) {
        Long versionId = dto.getVersionId();
        checkStatus(versionId, SceneConst.SCENE_VERSION_STATUS__CONFIGURING);
        return versionProcessService.save(dto);
    }

    @Override
    public void deleteProcess(VersionProcessDto dto) {
        Long versionId = dto.getVersionId();
        Assert.notNull(versionId, "版本id不能为空");

        SceneVersionDto versionDto = fetchById(ListUtil.of(versionId)).get(versionId);
        Assert.notNull(versionDto, "版本id不存在。id: {}", versionId);


    }

    @Override
    public SceneVersionDto debug(Long id, Long envId) {
        Assert.notNull(id, "版本id不能为空");
        SceneVersionDto sceneVersionDto = fetchById(ListUtil.of(id)).get(id);
        Assert.notNull(sceneVersionDto, "版本不存在。version id：{}", id);
        List<VersionProcessDto> versionProcessDtoList = versionProcessService.fetchVersionProcessListByVersionId(ListUtil.of(id), true).get(id);
        Assert.isTrue(CollUtil.isEmpty(versionProcessDtoList), "该版本下至少需要配置一个流程");

        Assert.notNull(envId, "环境id不能为空");

        for (VersionProcessDto versionProcessDto : versionProcessDtoList) {
            BizDeployDto deployDto = new BizDeployDto();
            deployDto.setProcessId(versionProcessDto.getProcessId());
            deployDto.setProcessName(versionProcessDto.getProcessName());
            deployDto.setProcessXml(versionProcessDto.getProcessXml());
            processDeployService.snapshotDeploy(deployDto, envId, ProcessConst.PROCESS_BIZ_TYPE__SCENE);
        }

        // 修改状态
        sceneVersionDto.setStatus(SceneConst.SCENE_VERSION_STATUS__DEBUGGING);
        sceneVersionDao.updateById(SceneVersionPoConverter.createFrom(sceneVersionDto));
        return sceneVersionDto;
    }

    @Override
    public Map<Long, Long> countById(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
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
        if(CollUtil.isEmpty(idList)) return MapUtil.empty();
        Assert.isTrue(idList.stream().filter(Objects::isNull).count() == 0, "版本id不能为空");
        QueryWrapper<SceneVersionPo> query = new QueryWrapper<>();
        query.in(SceneVersionPo.ID, idList);
        List<SceneVersionPo> sceneVersionPos = sceneVersionDao.selectList(query);
        return sceneVersionPos.stream().map(SceneVersionDtoConverter::from)
                .collect(Collectors.toMap(SceneVersionDto::getId, Function.identity()));
    }

    private void checkStatus(Long id, int status) {
        Assert.notNull(id, "版本id不能为空");

        SceneVersionDto versionDto = fetchById(ListUtil.of(id)).get(id);
        Assert.notNull(versionDto, "流程id不存在。id: {}", id);

        if(!NumberUtil.equals(versionDto.getStatus(), status)) {
            throw new RuntimeException(StrUtil.format("版本状态异常，id: {}", id));
        }
    }

}
