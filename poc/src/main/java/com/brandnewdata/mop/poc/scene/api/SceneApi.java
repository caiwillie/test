package com.brandnewdata.mop.poc.scene.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.api.scene.ISceneApi;
import com.brandnewdata.mop.api.scene.dto.SceneQuery;
import com.brandnewdata.mop.api.scene.dto.VersionProcessStartDto;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService2;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.service.ISceneService;
import com.brandnewdata.mop.poc.scene.service.IVersionProcessService;
import com.dxy.library.json.jackson.JacksonUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SceneApi implements ISceneApi {

    private final ISceneService service;

    private final IVersionProcessService versionProcessService;

    private final IProcessDeployService2 processDeployService;

    public SceneApi(ISceneService service,
                    IVersionProcessService versionProcessService,
                    IProcessDeployService2 processDeployService) {
        this.service = service;
        this.versionProcessService = versionProcessService;
        this.processDeployService = processDeployService;
    }

    @Override
    public Result<List<com.brandnewdata.mop.api.scene.dto.SceneDto>> listByIds(SceneQuery req) {
        return null;
    }

    @Override
    public Result startVersionProcessAsync(List<VersionProcessStartDto> startDtoList) {
        Result ret = Result.OK();
        if(CollUtil.isEmpty(startDtoList)) return ret;

        VersionProcessStartDto versionProcessStartDto = startDtoList.get(0);

        SceneReleaseDeployDto sceneReleaseDeployDto =
                JacksonUtil.from(versionProcessStartDto.getProcessRelevantInfo(), SceneReleaseDeployDto.class);
        Assert.notNull(sceneReleaseDeployDto, "流程相关配置不能为空");

        String processId = sceneReleaseDeployDto.getProcessId();
        Long envId = sceneReleaseDeployDto.getEnvId();
        Map<String, Object> variables = Opt.ofNullable(JacksonUtil.fromMap(versionProcessStartDto.getContent())).orElse(MapUtil.empty());

        processDeployService.startAsync(processId, variables, envId);

        return ret;
    }

    @Override
    public Result startVersionProcessSync(List<VersionProcessStartDto> startDtoList) {
        Result ret = Result.OK();
        if(CollUtil.isEmpty(startDtoList)) return ret;

        VersionProcessStartDto versionProcessStartDto = startDtoList.get(0);

        SceneReleaseDeployDto sceneReleaseDeployDto =
                JacksonUtil.from(versionProcessStartDto.getProcessRelevantInfo(), SceneReleaseDeployDto.class);
        Assert.notNull(sceneReleaseDeployDto, "流程相关配置不能为空");

        String processId = sceneReleaseDeployDto.getProcessId();
        Long envId = sceneReleaseDeployDto.getEnvId();
        Map<String, Object> variables = Opt.ofNullable(JacksonUtil.fromMap(versionProcessStartDto.getContent())).orElse(MapUtil.empty());

        VersionProcessDto versionProcessDto = versionProcessService.fetchOneByProcessId(ListUtil.of(processId)).get(processId);
        BpmnXmlDto bpmnXmlDto = new BpmnXmlDto();
        bpmnXmlDto.setProcessId(versionProcessDto.getProcessId());
        bpmnXmlDto.setProcessName(versionProcessDto.getProcessName());
        bpmnXmlDto.setProcessXml(versionProcessDto.getProcessXml());

        Map<String, Object> data = processDeployService.startSync(bpmnXmlDto, variables, envId, ProcessConst.PROCESS_BIZ_TYPE__SCENE);
        return Result.OK(Opt.ofNullable(data).orElse(MapUtil.empty()));
    }

}
