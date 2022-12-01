package com.brandnewdata.mop.poc.bff.service.scene;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneDeployVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.SceneVersionDeployVo;
import com.brandnewdata.mop.poc.bff.vo.scene.operate.VersionProcessDeployVo;
import com.brandnewdata.mop.poc.operate.service.IProcessInstanceService2;
import com.brandnewdata.mop.poc.scene.dto.SceneReleaseDeployDto;
import com.brandnewdata.mop.poc.scene.service.ISceneReleaseDeployService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SceneOperateBffService2 {

    private final ISceneReleaseDeployService sceneReleaseDeployService;

    @Resource
    private IProcessInstanceService2 processInstanceService;

    public SceneOperateBffService2(ISceneReleaseDeployService sceneReleaseDeployService) {
        this.sceneReleaseDeployService = sceneReleaseDeployService;
    }



}