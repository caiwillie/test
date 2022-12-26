package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionExportDto;
import com.brandnewdata.mop.poc.scene.dto.external.ConfirmLoadDto;
import com.brandnewdata.mop.poc.scene.dto.external.PrepareLoadDto;

import java.util.List;

public interface IDataExternalService {

    SceneVersionExportDto export(Long versionId, List<String> processIdList);

    PrepareLoadDto prepareLoad(byte[] bytes);

    SceneVersionDto confirmLoad(ConfirmLoadDto confirmLoadDto);
}
