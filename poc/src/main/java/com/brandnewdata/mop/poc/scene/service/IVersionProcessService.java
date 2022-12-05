package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.List;
import java.util.Map;

public interface IVersionProcessService {

    Map<Long, List<VersionProcessDto>> fetchVersionProcessListByVersionId(List<Long> versionIdList, boolean simple);

    Map<Long, VersionProcessDto> fetchVersionProcessById(List<Long> idList);

    Map<String, VersionProcessDto> fetchVersionProcessByProcessId(List<String> processIdList);

    Map<Long, Integer> fetchVersionProcessCountByVersionId(List<Long> versionIdList);

    Map<Long, VersionProcessDto> fetchLatestProcessByVersionId(List<Long> versionIdList);

    VersionProcessDto save(VersionProcessDto versionProcessDto);


}
