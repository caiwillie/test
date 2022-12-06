package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.List;
import java.util.Map;

public interface IVersionProcessService {

    Map<Long, List<VersionProcessDto>> fetchListByVersionId(List<Long> versionIdList, boolean simple);

    Map<Long, VersionProcessDto> fetchOneById(List<Long> idList);

    Map<String, VersionProcessDto> fetchOneByProcessId(List<String> processIdList);

    Map<Long, Integer> fetchCountByVersionId(List<Long> versionIdList);

    Map<Long, VersionProcessDto> fetchLatestOneByVersionId(List<Long> versionIdList);

    VersionProcessDto save(VersionProcessDto versionProcessDto);

    void deleteById(List<Long> idList);
    

}
