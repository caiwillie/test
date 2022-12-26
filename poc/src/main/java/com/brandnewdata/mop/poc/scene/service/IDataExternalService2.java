package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionExportDto;
import com.brandnewdata.mop.poc.scene.dto.external.PrepareLoadDto;

import java.util.List;

public interface IDataExternalService2 {

    public SceneVersionExportDto export(Long versionId, List<String> processIdList);

    public PrepareLoadDto prepareLoad(byte[] bytes);

}
