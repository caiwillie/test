package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto;
import com.brandnewdata.mop.poc.scene.dto.SceneProcessDto2;

import java.util.List;

public interface ISceneProcessService {

    List<SceneProcessDto2> listByProcessIdList(List<String> processIdList);
}
